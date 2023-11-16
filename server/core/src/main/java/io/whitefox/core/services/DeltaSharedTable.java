package io.whitefox.core.services;

import io.delta.standalone.DeltaLog;
import io.delta.standalone.Snapshot;
import io.whitefox.core.*;
import io.whitefox.core.delta.Metadata;
import io.whitefox.core.delta.Protocol;
import io.whitefox.core.results.ReadTableResultToBeSigned;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

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

  public Optional<Metadata> getMetadata(
      Optional<String> startingTimestamp, DeltaSharingCapabilities deltaSharingCapabilities) {
    return getSnapshot(startingTimestamp)
        .map(snapshot -> metadataFromSnapshot(snapshot, deltaSharingCapabilities));
  }

  private Metadata metadataFromSnapshot(
      Snapshot snapshot, DeltaSharingCapabilities requestCapabilities) {

    return new Metadata(
        snapshot.getMetadata().getId(),
        Optional.of(tableDetails.name()),
        Optional.ofNullable(snapshot.getMetadata().getDescription()),
        Metadata.Format.PARQUET, // TODO this depends on the table
        new TableSchema(tableSchemaConverter.convertDeltaSchemaToWhitefox(
            snapshot.getMetadata().getSchema())),
        snapshot.getMetadata().getPartitionColumns(),
        snapshot.getMetadata().getConfiguration(),
        snapshot.getVersion(),
        Optional.empty(), // size is fine to be empty
        Optional.empty(), // numFiles is ok to be empty here too
        compatibleCapabilities(
            DeltaSharingCapabilities.defaultValue(),
            requestCapabilities) // TODO read it from the table
        );
  }

  private DeltaSharingCapabilities compatibleCapabilities(
      DeltaSharingCapabilities tableCapabilities, DeltaSharingCapabilities requestCapabilities) {
    if (requestCapabilities
        .getResponseFormat()
        .contains(DeltaSharingCapabilities.DeltaSharingResponseFormat.DELTA)) {
      return tableCapabilities.withResponseFormat(
          DeltaSharingCapabilities.DeltaSharingResponseFormat.DELTA);
    } else {
      return tableCapabilities.withResponseFormat(
          DeltaSharingCapabilities.DeltaSharingResponseFormat.PARQUET);
    }
  }

  public Optional<Long> getTableVersion(Optional<String> startingTimestamp) {
    return getSnapshot(startingTimestamp).map(Snapshot::getVersion);
  }

  public ReadTableResultToBeSigned queryTable(
      ReadTableRequest readTableRequest, DeltaSharingCapabilities requestCapabilities) {
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
        new Protocol(Optional.of(1), Optional.of(1)), // TODO
        metadataFromSnapshot(snapshot, requestCapabilities),
        snapshot.getAllFiles().stream()
            .map(f -> new TableFileToBeSigned(
                location() + "/" + f.getPath(),
                f.getSize(),
                snapshot.getVersion(),
                snapshot.getMetadata().getCreatedTime(),
                f.getStats(),
                f.getPartitionValues()))
            .collect(Collectors.toList()));
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

  private String location() {
    // remove all "/" at the end of the path
    return location.replaceAll("/+$", "");
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
