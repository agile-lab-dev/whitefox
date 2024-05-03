package io.whitefox.core.services;

import static io.whitefox.core.PredicateUtils.evaluateJsonPredicate;
import static io.whitefox.core.PredicateUtils.evaluateSqlPredicate;

import io.delta.kernel.Snapshot;
import io.delta.kernel.Table;
import io.delta.kernel.TableNotFoundException;
import io.delta.kernel.client.TableClient;
import io.delta.kernel.defaults.client.DefaultTableClient;
import io.delta.kernel.internal.snapshot.SnapshotManager;
import io.whitefox.core.*;
import io.whitefox.core.Metadata;
import io.whitefox.core.TableSchema;
import io.whitefox.core.services.capabilities.ResponseFormat;
import io.whitefox.core.types.predicates.PredicateException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

public class DeltaSharedTable implements InternalSharedTable {

  private final Logger logger = Logger.getLogger(this.getClass());

  private final TableClient tableClient;
  private final Table table;
  private final TableSchemaConverter tableSchemaConverter;
  private final SharedTable tableDetails;
  private final String location;

  private DeltaSharedTable(
      TableClient tableClient,
      Table table,
      TableSchemaConverter tableSchemaConverter,
      SharedTable sharedTable,
      String location) {
    this.tableClient = tableClient;
    this.table = table;
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
      var tableClient = DefaultTableClient.create(
          hadoopConfigBuilder.buildConfig(sharedTable.internalTable().provider().storage()));
      Table dt = null;
      try {
        dt = Table.forPath(tableClient, dataPath);
      } catch (TableNotFoundException e) {
        throw new IllegalArgumentException(
            String.format("Cannot find a delta table at %s", dataPath), e);
      }
      return new DeltaSharedTable(tableClient, dt, tableSchemaConverter, sharedTable, dataPath);
    } else {
      throw new IllegalArgumentException(
          String.format("%s is not a delta table", sharedTable.name()));
    }
  }

  public static DeltaSharedTable of(SharedTable sharedTable) {
    return of(sharedTable, TableSchemaConverter.INSTANCE, new HadoopConfigBuilder());
  }

  public Optional<Metadata> getMetadata(Optional<Timestamp> startingTimestamp) {
    return getSnapshot(startingTimestamp).map(this::metadataFromSnapshot);
  }

  private Metadata metadataFromSnapshot(Snapshot snapshot) {
    return new Metadata(
        snapshot.getMetadata().getId(),
        Optional.of(tableDetails.name()),
        Optional.ofNullable(snapshot.getMetadata().getDescription()),
        ResponseFormat.parquet,
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

  private boolean filterFilesBasedOnSqlPredicates(
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

  private boolean filterFilesBasedOnJsonPredicates(Optional<String> predicates, AddFile f) {
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

  public ReadTableResultToBeSigned queryTable(ReadTableRequest readTableRequest)
      throws TableNotFoundException {
    Optional<String> predicates;
    Optional<List<String>> sqlPredicates;
    Snapshot snapshot;
    if (readTableRequest instanceof ReadTableRequest.ReadTableCurrentVersion) {
      snapshot = table.getLatestSnapshot(tableClient);
      predicates =
          ((ReadTableRequest.ReadTableCurrentVersion) readTableRequest).jsonPredicateHints();
      sqlPredicates =
          ((ReadTableRequest.ReadTableCurrentVersion) readTableRequest).predicateHints();
    } else if (readTableRequest instanceof ReadTableRequest.ReadTableAsOfTimestamp) {
      SnapshotManager mng = null;
      snapshot = deltaLog.getSnapshotForTimestampAsOf(
          ((ReadTableRequest.ReadTableAsOfTimestamp) readTableRequest).timestamp());
      predicates =
          ((ReadTableRequest.ReadTableAsOfTimestamp) readTableRequest).jsonPredicateHints();
      sqlPredicates = ((ReadTableRequest.ReadTableAsOfTimestamp) readTableRequest).predicateHints();
    } else if (readTableRequest instanceof ReadTableRequest.ReadTableVersion) {
      snapshot = deltaLog.getSnapshotForVersionAsOf(
          ((ReadTableRequest.ReadTableVersion) readTableRequest).version());
      predicates = ((ReadTableRequest.ReadTableVersion) readTableRequest).jsonPredicateHints();
      sqlPredicates = ((ReadTableRequest.ReadTableVersion) readTableRequest).predicateHints();
    } else {
      throw new IllegalArgumentException("Unknown ReadTableRequest type: " + readTableRequest);
    }
    var metadata = metadataFromSnapshot(snapshot);
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
        snapshot.getVersion());
  }

  private Optional<Snapshot> getSnapshot(Optional<Timestamp> startingTimestamp) {
    return startingTimestamp
        .map(Timestamp::getTime)
        .map(this::getSnapshotForTimestampAsOf)
        .orElse(Optional.of(getSnapshot()));
  }

  private Snapshot getSnapshot() {
    return deltaLog.snapshot();
  }

  private Optional<Snapshot> getSnapshotForTimestampAsOf(long l) {
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
