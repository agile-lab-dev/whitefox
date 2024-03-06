package io.whitefox.core.services;

import io.whitefox.core.*;
import io.whitefox.core.services.capabilities.ClientCapabilities;
import io.whitefox.core.services.capabilities.ResponseFormat;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.apache.iceberg.util.SnapshotUtil;

public class IcebergSharedTable implements InternalSharedTable {

  private final Table icebergTable;
  private final TableSchemaConverter tableSchemaConverter;

  private IcebergSharedTable(Table icebergTable, TableSchemaConverter tableSchemaConverter) {
    this.icebergTable = icebergTable;
    this.tableSchemaConverter = tableSchemaConverter;
  }

  public static IcebergSharedTable of(
      Table icebergTable, TableSchemaConverter tableSchemaConverter) {
    return new IcebergSharedTable(icebergTable, tableSchemaConverter);
  }

  public static IcebergSharedTable of(Table icebergTable) {
    return new IcebergSharedTable(icebergTable, new TableSchemaConverter());
  }

  private Metadata getMetadataFromSnapshot(Snapshot snapshot) {
    return new Metadata(
        String.valueOf(snapshot.snapshotId()),
        Optional.of(icebergTable.name()),
        Optional.empty(),
        FileFormat.parquet,
        new TableSchema(tableSchemaConverter.convertIcebergSchemaToWhitefox(
            icebergTable.schema().asStruct())),
        icebergTable.spec().fields().stream()
            .map(PartitionField::name)
            .collect(Collectors.toList()),
        icebergTable.properties(),
        Optional.of(snapshot.sequenceNumber()),
        Optional.empty(), // size is fine to be empty
        Optional.empty() // numFiles is ok to be empty here too
        );
  }

  private Optional<Snapshot> getSnapshot(Optional<Timestamp> startingTimestamp) {
    return startingTimestamp
        .map(Timestamp::getTime)
        .map(this::getSnapshotForTimestampAsOf)
        .orElseGet(() -> Optional.ofNullable(icebergTable.currentSnapshot()));
  }

  private Optional<Snapshot> getSnapshotForTimestampAsOf(long l) {
    try {
      return Optional.of(SnapshotUtil.snapshotIdAsOfTime(icebergTable, l))
          .map(icebergTable::snapshot);
    } catch (IllegalArgumentException iea) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<Long> getTableVersion(Optional<Timestamp> startingTimestamp) {
    return getSnapshot(startingTimestamp).map(Snapshot::sequenceNumber);
  }

  @Override
  public ReadTableResultToBeSigned queryTable(ReadTableRequest readTableRequest) {
    throw new NotImplementedException();
  }

  @Override
  public Optional<MetadataResponse> getMetadata(
      Optional<Timestamp> startingTimestamp, ClientCapabilities clientCapabilities) {
    return getSnapshot(startingTimestamp)
        .map(s -> new MetadataResponse(getMetadataFromSnapshot(s), ResponseFormat.parquet));
  }
}
