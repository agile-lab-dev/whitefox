package io.whitefox.core.services;

import static io.whitefox.core.PredicateUtils.evaluateJsonPredicate;
import static io.whitefox.core.PredicateUtils.evaluateSqlPredicate;

import io.delta.standalone.DeltaLog;
import io.delta.standalone.Snapshot;
import io.delta.standalone.actions.AddFile;
import io.delta.standalone.internal.DeltaLogImpl;
import io.delta.standalone.internal.SnapshotImpl;
import io.whitefox.core.*;
import io.whitefox.core.Metadata;
import io.whitefox.core.TableSchema;
import io.whitefox.core.services.capabilities.ClientCapabilities;
import io.whitefox.core.services.capabilities.ResponseFormat;
import io.whitefox.core.services.exceptions.IncompatibleTableWithClient;
import io.whitefox.core.types.predicates.PredicateException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;

public class DeltaSharedTable implements InternalSharedTable {

  private final Logger logger = Logger.getLogger(this.getClass());

  private final DeltaLogImpl deltaLog;
  private final TableSchemaConverter tableSchemaConverter;
  private final SharedTable tableDetails;
  private final String location;

  private DeltaSharedTable(
      DeltaLogImpl deltaLog,
      TableSchemaConverter tableSchemaConverter,
      SharedTable sharedTable,
      String location) {
    this.deltaLog = deltaLog;
    this.tableSchemaConverter = tableSchemaConverter;
    this.tableDetails = sharedTable;
    this.location = location;
  }

  public static DeltaSharedTable of(
      SharedTable sharedTable,
      TableSchemaConverter tableSchemaConverter,
      HadoopConfigBuilder hadoopConfigBuilder) {

    if (sharedTable.internalTable().properties() instanceof InternalTable.DeltaTableProperties) {
      InternalTable.DeltaTableProperties deltaProps =
          (InternalTable.DeltaTableProperties) sharedTable.internalTable().properties();
      var dataPath = deltaProps.location();
      var dt = DeltaLog.forTable(
          hadoopConfigBuilder.buildConfig(sharedTable.internalTable().provider().storage()),
          dataPath);
      if (!dt.tableExists()) {
        throw new IllegalArgumentException(
            String.format("Cannot find a delta table at %s", dataPath));
      }
      return new DeltaSharedTable((DeltaLogImpl) dt, tableSchemaConverter, sharedTable, dataPath);
    } else {
      throw new IllegalArgumentException(
          String.format("%s is not a delta table", sharedTable.name()));
    }
  }

  public static DeltaSharedTable of(SharedTable sharedTable) {
    return of(sharedTable, TableSchemaConverter.INSTANCE, new HadoopConfigBuilder());
  }

  @Override
  public Optional<MetadataResponse> getMetadata(
      Optional<Timestamp> startingTimestamp, ClientCapabilities clientCapabilities) {
    return getSnapshot(startingTimestamp).map(snapshot -> {
      final ResponseFormat responseFormat = chooseResponseFormat(snapshot, clientCapabilities);
      return new MetadataResponse(metadataFromSnapshot(snapshot), responseFormat);
    });
  }

  private Metadata metadataFromSnapshot(Snapshot snapshot) {
    return new Metadata(
        snapshot.getMetadata().getId(),
        Optional.of(tableDetails.name()),
        Optional.ofNullable(snapshot.getMetadata().getDescription()),
        FileFormat.parquet,
        new TableSchema(tableSchemaConverter.convertDeltaSchemaToWhitefox(
            snapshot.getMetadata().getSchema())),
        snapshot.getMetadata().getPartitionColumns(),
        snapshot.getMetadata().getConfiguration(),
        Optional.of(snapshot.getVersion()),
        Optional.empty(), // size is fine to be empty
        Optional.empty() // numFiles is ok to be empty here too
        );
  }

  public Optional<Long> getTableVersion(Optional<Timestamp> startingTimestamp) {
    return getSnapshot(startingTimestamp).map(Snapshot::getVersion);
  }

  public boolean filterFilesBasedOnSqlPredicates(
      Optional<List<String>> predicates, AddFile f, Metadata metadata) {
    // if there are no predicates return all possible files
    if (predicates.isEmpty()) {
      return true;
    }
    try {
      var ctx = PredicateUtils.createEvalContext(f);
      return predicates.get().stream().allMatch(p -> evaluateSqlPredicate(p, ctx, f, metadata));
    } catch (PredicateException e) {
      logger.debug("Caught exception: " + e.getMessage());
      logger.info("File: " + f.getPath()
          + " will be used in processing due to failure in parsing or processing the predicate");
      return true;
    }
  }

  public boolean filterFilesBasedOnJsonPredicates(Optional<String> predicates, AddFile f) {
    // if there are no predicates return all possible files
    if (predicates.isEmpty()) {
      return true;
    }
    try {
      var ctx = PredicateUtils.createEvalContext(f);
      return evaluateJsonPredicate(predicates, ctx, f);
    } catch (PredicateException e) {
      logger.debug("Caught exception: " + e.getMessage());
      logger.info("File: " + f.getPath()
          + " will be used in processing due to failure in parsing or processing the predicate");
      return true;
    }
  }

  private ResponseFormat chooseResponseFormat(
      SnapshotImpl snapshotImpl, ClientCapabilities clientCapabilities) {
    return _chooseResponseFormat(snapshotImpl, clientCapabilities)
        .orElseThrow(() -> new IncompatibleTableWithClient(String.format(
            "Table %s cannot be read by client with capabilities %s",
            tableDetails.description(), clientCapabilities)));
  }

  private Optional<ResponseFormat> _chooseResponseFormat(
      SnapshotImpl snapshotImpl, ClientCapabilities clientCapabilities) {
    if (snapshotImpl.protocol().getMinReaderVersion() == 1
        && clientCapabilities.isCompatibleWith(ResponseFormat.parquet)) {
      return Optional.of(ResponseFormat.parquet);
    } else if (clientCapabilities.isCompatibleWith(ResponseFormat.delta)) {
      return Optional.of(ResponseFormat.delta);
    } else {
      return Optional.empty();
    }
  }

  public ReadTableResultToBeSigned queryTable(ReadTableRequest readTableRequest) {
    final Optional<String> predicates = readTableRequest.jsonPredicateHints();
    final Optional<List<String>> sqlPredicates = readTableRequest.predicateHints();
    SnapshotImpl snapshot;
    if (readTableRequest instanceof ReadTableRequest.ReadTableCurrentVersion) {
      snapshot = deltaLog.snapshot();
    } else if (readTableRequest instanceof ReadTableRequest.ReadTableAsOfTimestamp) {
      snapshot = deltaLog.getSnapshotForTimestampAsOf(
          ((ReadTableRequest.ReadTableAsOfTimestamp) readTableRequest).timestamp());
    } else if (readTableRequest instanceof ReadTableRequest.ReadTableVersion) {
      snapshot = deltaLog.getSnapshotForVersionAsOf(
          ((ReadTableRequest.ReadTableVersion) readTableRequest).version());
    } else {
      throw new IllegalArgumentException("Unknown ReadTableRequest type: " + readTableRequest);
    }

    final ResponseFormat responseFormat =
        chooseResponseFormat(snapshot, readTableRequest.clientCapabilities());
    if (ResponseFormat.parquet == responseFormat) {
      final var metadata = metadataFromSnapshot(snapshot);
      return new ReadTableResultToBeSigned(
          new Protocol(Optional.of(1)),
          metadata,
          snapshot.getAllFiles().stream()
              .filter(f -> filterFilesBasedOnJsonPredicates(predicates, f))
              .filter(f -> filterFilesBasedOnSqlPredicates(sqlPredicates, f, metadata))
              .map(f -> new TableFileToBeSigned(
                  location() + "/" + f.getPath(),
                  f.getSize(),
                  snapshot.getVersion(),
                  snapshot.getMetadata().getCreatedTime(),
                  f.getStats(),
                  f.getPartitionValues()))
              .collect(Collectors.toList()),
          snapshot.getVersion(),
          responseFormat);
    } else {
      throw new NotImplementedException(String.format(
          "Delta protocol is currently not implemented, "
              + "table %s can't be read by the current version of whitefox",
          tableDetails.description()));
    }
  }

  private Optional<SnapshotImpl> getSnapshot(Optional<Timestamp> startingTimestamp) {
    return startingTimestamp
        .map(Timestamp::getTime)
        .map(this::getSnapshotForTimestampAsOf)
        .orElse(Optional.of(getSnapshot()));
  }

  private SnapshotImpl getSnapshot() {
    return deltaLog.snapshot();
  }

  private Optional<SnapshotImpl> getSnapshotForTimestampAsOf(long l) {
    try {
      return Optional.of(deltaLog.getSnapshotForTimestampAsOf(l));
    } catch (IllegalArgumentException iea) {
      return Optional.empty();
    }
  }

  private String location() {
    // remove all "/" at the end of the path
    return location.replaceAll("/+$", "");
  }
}
