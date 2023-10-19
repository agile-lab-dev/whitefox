package io.whitefox.core.services;

import io.delta.standalone.DeltaLog;
import io.delta.standalone.Snapshot;
import io.whitefox.core.*;
import io.whitefox.core.Metadata;
import io.whitefox.core.TableSchema;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.hadoop.conf.Configuration;

public class DeltaSharedTable {

  private final DeltaLog deltaLog;
  private final TableSchemaConverter tableSchemaConverter;

  private DeltaSharedTable(DeltaLog deltaLog, TableSchemaConverter tableSchemaConverter) {
    this.deltaLog = deltaLog;
    this.tableSchemaConverter = tableSchemaConverter;
  }

  public static DeltaSharedTable of(Table table, TableSchemaConverter tableSchemaConverter) {
    var configuration = new Configuration();
    var dataPath = Paths.get(table.location());

    var dt = DeltaLog.forTable(configuration, dataPath.toString());
    var snap = dt.update();
    if (snap.getVersion() == -1) {
      throw new IllegalArgumentException(
          String.format("Cannot find a delta table at %s", dataPath));
    }
    return new DeltaSharedTable(dt, tableSchemaConverter);
  }

  public static DeltaSharedTable of(Table table) {
    return of(table, TableSchemaConverter.INSTANCE);
  }

  public Optional<Metadata> getMetadata(Optional<String> startingTimestamp) {
    return getSnapshot(startingTimestamp)
        .map(snapshot -> new Metadata(
            snapshot.getMetadata().getId(),
            Metadata.Format.PARQUET,
            new TableSchema(tableSchemaConverter.convertDeltaSchemaToWhitefox(
                snapshot.getMetadata().getSchema())),
            snapshot.getMetadata().getPartitionColumns()));
  }

  public Optional<Long> getTableVersion(Optional<String> startingTimestamp) {
    return getSnapshot(startingTimestamp).map(Snapshot::getVersion);
  }

  public ReadTableResultToBeSigned queryTable(ReadTableRequest readTableRequest) {
    Snapshot snapshot;
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
    return new ReadTableResultToBeSigned(
        new Protocol(Optional.empty()), // TODO
        new Metadata(
            snapshot.getMetadata().getId(),
            Metadata.Format.PARQUET,
            new TableSchema(tableSchemaConverter.convertDeltaSchemaToWhitefox(
                snapshot.getMetadata().getSchema())),
            snapshot.getMetadata().getPartitionColumns()),
        snapshot.getAllFiles().stream()
            .map(f -> new TableFileToBeSigned(f.getPath(), f.getSize(), f.getPartitionValues()))
            .collect(Collectors.toList()));
    //            TODO why these have gone after rebase?
    //
    //            snapshot.getMetadata().getId(),
    //            snapshot.getMetadata().getConfiguration(),
    //            Optional.of(
    //                snapshot.getAllFiles().stream().map(AddFile::getSize).reduce(0L, Long::sum)),
    //            Optional.of((long) snapshot.getAllFiles().size())),
    //        snapshot.getAllFiles().stream()
    //            .map(f -> new TableFileToBeSigned(
    //                f.getPath().toString(), f.getSize(), f.getPartitionValues()))
    //            .collect(Collectors.toList()));
  }

  private Optional<Snapshot> getSnapshot(Optional<String> startingTimestamp) {
    return startingTimestamp
        .map(this::getTimestamp)
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

  private Timestamp getTimestamp(String timestamp) {
    return new Timestamp(OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        .toInstant()
        .toEpochMilli());
  }

  public static class DeltaShareTableFormat {
    public static final String RESPONSE_FORMAT_PARQUET = "parquet";
    public static final String RESPONSE_FORMAT_DELTA = "delta";
  }
}
