package io.whitefox.core.services;

import io.whitefox.core.*;
import io.whitefox.core.Metadata;
import io.whitefox.core.ReadTableRequest;
import io.whitefox.core.ReadTableResultToBeSigned;
import io.whitefox.core.TableSchema;
import io.whitefox.core.services.capabilities.ResponseFormat;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.apache.iceberg.util.SnapshotUtil;

public class IcebergSharedTable implements InternalSharedTable {

  private final Table icebergTable;
  private final TableSchemaConverter tableSchemaConverter;
  private final SharedTable tableDetails;
  private final FileIOFactory fileIOFactory;

  private IcebergSharedTable(
      Table icebergTable,
      TableSchemaConverter tableSchemaConverter,
      SharedTable tableDetails,
      FileIOFactory fileIOFactory) {
    this.icebergTable = icebergTable;
    this.tableSchemaConverter = tableSchemaConverter;
    this.tableDetails = tableDetails;
    this.fileIOFactory = fileIOFactory;
  }

  public static IcebergSharedTable of(
      Table icebergTable, SharedTable tableDetails, TableSchemaConverter tableSchemaConverter) {
    return new IcebergSharedTable(
        icebergTable, tableSchemaConverter, tableDetails, new FileIOFactoryImpl());
  }

  public static IcebergSharedTable of(Table icebergTable, SharedTable tableDetails) {
    return new IcebergSharedTable(
        icebergTable, new TableSchemaConverter(), tableDetails, new FileIOFactoryImpl());
  }

  public Optional<Metadata> getMetadata(Optional<Timestamp> startingTimestamp) {
    return getSnapshot(startingTimestamp).map(this::getMetadataFromSnapshot);
  }

  private Metadata getMetadataFromSnapshot(Snapshot snapshot) {
    return new Metadata(
        String.valueOf(snapshot.snapshotId()),
        Optional.of(icebergTable.name()),
        Optional.empty(),
        ResponseFormat.parquet,
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
    Snapshot snapshot;
    if (readTableRequest instanceof ReadTableRequest.ReadTableCurrentVersion) {
      snapshot = icebergTable.currentSnapshot();
    } else if (readTableRequest instanceof ReadTableRequest.ReadTableAsOfTimestamp) {
      snapshot = icebergTable.snapshot(SnapshotUtil.snapshotIdAsOfTime(
          icebergTable, ((ReadTableRequest.ReadTableAsOfTimestamp) readTableRequest).timestamp()));
    } else if (readTableRequest instanceof ReadTableRequest.ReadTableVersion) {
      snapshot =
          icebergTable.snapshot(((ReadTableRequest.ReadTableVersion) readTableRequest).version());
    } else {
      throw new IllegalArgumentException("Unknown ReadTableRequest type: " + readTableRequest);
    }
    try (var s3FileIO = fileIOFactory.newFileIO(
        tableDetails.internalTable().provider().storage(),
        tableDetails
            .internalTable()
            .provider()
            .metastore()
            .orElseThrow(() -> new RuntimeException("metastore not found")))) {
      return new ReadTableResultToBeSigned(
          new Protocol(Optional.of(1)),
          getMetadataFromSnapshot(snapshot),
          StreamSupport.stream(snapshot.addedDataFiles(s3FileIO).spliterator(), false)
              .map(
                  dataFile -> new TableFileToBeSigned(
                      dataFile.path().toString(),
                      dataFile.fileSizeInBytes(),
                      snapshot.sequenceNumber(),
                      Optional.of(snapshot.timestampMillis()),
                      buildStats(
                          dataFile.recordCount(),
                          dataFile.lowerBounds(),
                          dataFile.upperBounds(),
                          dataFile.nullValueCounts()), // TODO understand how to build stats
                      Map.of()) // TODO understand how to retrieve partition values
                  )
              .collect(Collectors.toList()),
          snapshot.sequenceNumber());
    }
  }

  // TODO: find a way to handle the ByteBuffer values in a general way
  // TODO: find a way to retrieve the column names (to be consistent with delta)
  // TODO: factor out this code in a dedicated IcebergSharedTableStatsBuilder utility class
  private String buildStats(
      long recordCount,
      Map<Integer, ByteBuffer> lowerBounds,
      Map<Integer, ByteBuffer> upperBounds,
      Map<Integer, Long> nullValueCounts) {
    var minValues = lowerBounds.entrySet().stream()
        .map(e -> String.format(
            "\"%s\":%s", e.getKey().toString(), e.getValue().asIntBuffer().get()))
        .reduce((x, y) -> String.format("%s,%s", x, y))
        .orElse("");
    var maxValues = upperBounds.entrySet().stream()
        .map(e -> String.format(
            "\"%s\":%s", e.getKey().toString(), e.getValue().asIntBuffer().get()))
        .reduce((x, y) -> String.format("%s,%s", x, y))
        .orElse("");
    var nullCount = nullValueCounts.entrySet().stream()
        .map(e -> String.format("\"%s\":%d", e.getKey().toString(), e.getValue()))
        .reduce((x, y) -> String.format("%s,%s", x, y))
        .orElse("");
    return String.format(
        "{\"numRecords\":%d,\"minValues\":{%s},\"maxValues\":{%s},\"nullCount\":{%s}}",
        recordCount, minValues, maxValues, nullCount);
  }
}
