package io.whitefox.core.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.delta.standalone.DeltaLog;
import io.delta.standalone.Snapshot;
import io.delta.standalone.actions.AddFile;
import io.whitefox.core.*;
import io.whitefox.core.Metadata;
import io.whitefox.core.TableSchema;
import io.whitefox.core.types.predicates.BaseOp;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import io.whitefox.core.types.predicates.BaseOp;
import org.apache.hadoop.conf.Configuration;

public class DeltaSharedTable implements InternalSharedTable {

  private final DeltaLog deltaLog;
  private final TableSchemaConverter tableSchemaConverter;
  private final SharedTable tableDetails;
  private final String location;

  private DeltaSharedTable(
      DeltaLog deltaLog,
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
      return new DeltaSharedTable(dt, tableSchemaConverter, sharedTable, dataPath);
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
        Metadata.Format.PARQUET,
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

  public static BaseOp parsePredicate(String predicate) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(predicate, BaseOp.class);
    } catch (JsonProcessingException e) {
      System.out.println("cant parse predicate");
      throw e;
    }
  }

  public boolean filterFileBasedOnPredicates(List<String> predicates, AddFile f) {
    if (predicates == null) {
      return true;
    }
    var ctx = JsonPredicatesUtils.createEvalContext(f);
    return predicates.stream().allMatch(p -> {
      try {
        var parsedPredicate = JsonPredicatesUtils.parsePredicate(p);
        return parsedPredicate.evalExpectBoolean(ctx);
      } catch (JsonProcessingException e) {
        System.out.println("Unable to parse predicate: " + p + " due to: " + e);
        return false;
      }
    });
  }

  public ReadTableResultToBeSigned queryTable(ReadTableRequest readTableRequest) {
    List<String> predicates;
    Snapshot snapshot;
    if (readTableRequest instanceof ReadTableRequest.ReadTableCurrentVersion) {
      snapshot = deltaLog.snapshot();
      predicates = ((ReadTableRequest.ReadTableCurrentVersion) readTableRequest).predicateHints();
    } else if (readTableRequest instanceof ReadTableRequest.ReadTableAsOfTimestamp) {
      snapshot = deltaLog.getSnapshotForTimestampAsOf(
          ((ReadTableRequest.ReadTableAsOfTimestamp) readTableRequest).timestamp());
      predicates = ((ReadTableRequest.ReadTableAsOfTimestamp) readTableRequest).predicateHints();
    } else if (readTableRequest instanceof ReadTableRequest.ReadTableVersion) {
      snapshot = deltaLog.getSnapshotForVersionAsOf(
          ((ReadTableRequest.ReadTableVersion) readTableRequest).version());
      predicates = ((ReadTableRequest.ReadTableVersion) readTableRequest).predicateHints();
    } else {
      throw new IllegalArgumentException("Unknown ReadTableRequest type: " + readTableRequest);
    }
    return new ReadTableResultToBeSigned(
        new Protocol(Optional.of(1)),
        metadataFromSnapshot(snapshot),
        snapshot.getAllFiles().stream()
            .filter(f -> filterFileBasedOnPredicates(predicates, f))
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

  public static class DeltaShareTableFormat {
    public static final String RESPONSE_FORMAT_PARQUET = "parquet";
    public static final String RESPONSE_FORMAT_DELTA = "delta";
  }
}
