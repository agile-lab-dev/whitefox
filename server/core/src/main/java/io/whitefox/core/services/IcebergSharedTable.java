package io.whitefox.core.services;

import io.whitefox.core.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.BaseMetastoreCatalog;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.Table;
import org.apache.iceberg.aws.glue.GlueCatalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.hadoop.HadoopCatalog;

public class IcebergSharedTable implements AbstractSharedTable {

  private final Table icebergTable;
  private final TableSchemaConverter tableSchemaConverter;

  private IcebergSharedTable(Table icebergTable, TableSchemaConverter tableSchemaConverter) {
    this.icebergTable = icebergTable;
    this.tableSchemaConverter = tableSchemaConverter;
  }

  public static IcebergSharedTable of(
      SharedTable tableDetails,
      TableSchemaConverter tableSchemaConverter,
      HadoopConfigBuilder hadoopConfigBuilder) {

    if (tableDetails.internalTable().properties() instanceof InternalTable.IcebergTableProperties) {
      var metastore = getMetastore(tableDetails.internalTable());
      var catalog = newCatalog(
          metastore,
          hadoopConfigBuilder,
          tableDetails.internalTable().provider().storage());
      var tableId = getTableIdentifier(tableDetails.internalTable());
      try {
        return new IcebergSharedTable(catalog.loadTable(tableId), tableSchemaConverter);
      } catch (Exception e) {
        throw new IllegalArgumentException(String.format(
            "Cannot found iceberg table [%s] under namespace [%s]",
            tableId.name(), tableId.namespace()));
      }
    } else {
      throw new IllegalArgumentException(
          String.format("%s is not an iceberg table", tableDetails.name()));
    }
  }

  private static TableIdentifier getTableIdentifier(InternalTable internalTable) {
    var icebergTableProperties =
        ((InternalTable.IcebergTableProperties) internalTable.properties());
    return TableIdentifier.of(
        icebergTableProperties.databaseName(), icebergTableProperties.tableName());
  }

  private static Metastore getMetastore(InternalTable internalTable) {
    return internalTable
        .provider()
        .metastore()
        .orElseThrow(() -> new RuntimeException(
            String.format("missing metastore for the iceberg table: [%s]", internalTable.name())));
  }

  private static BaseMetastoreCatalog newCatalog(
      Metastore metastore, HadoopConfigBuilder hadoopConfigBuilder, Storage storage) {
    if (metastore.type() == MetastoreType.GLUE) {
      try (var catalog = new GlueCatalog()) {
        Configuration conf = hadoopConfigBuilder.buildConfig(storage);
        catalog.setConf(conf);
        catalog.initialize(metastore.name(), setGlueProperties());
        return catalog;
      } catch (IOException e) {
        throw new RuntimeException("Unexpected exception when initializing the glue catalog", e);
      }
    } else if (metastore.type() == MetastoreType.HADOOP) {
      try (var catalog = new HadoopCatalog()) {
        Configuration conf = hadoopConfigBuilder.buildConfig(storage);
        catalog.setConf(conf);
        catalog.initialize(
            metastore.name(),
            Map.of(
                CatalogProperties.WAREHOUSE_LOCATION,
                ((MetastoreProperties.HadoopMetastoreProperties) metastore.properties())
                    .location()));
        return catalog;
      } catch (IOException e) {
        throw new RuntimeException("Unexpected exception when initializing the hadoop catalog", e);
      }
    } else {
      throw new RuntimeException(String.format("Unknown metastore type: [%s]", metastore.type()));
    }
  }

  public static IcebergSharedTable of(SharedTable sharedTable) {
    return of(sharedTable, new TableSchemaConverter(), new HadoopConfigBuilder());
  }

  public Optional<Metadata> getMetadata(Optional<String> startingTimestamp) {
    throw new NotImplementedException();
  }

  @Override
  public Optional<Long> getTableVersion(Optional<String> startingTimestamp) {
    throw new NotImplementedException();
  }

  @Override
  public ReadTableResultToBeSigned queryTable(ReadTableRequest readTableRequest) {
    throw new NotImplementedException();
  }

  private static Map<String, String> setGlueProperties() {
    Map<String, String> properties = new HashMap<>();
    properties.put(CatalogProperties.CATALOG_IMPL, "org.apache.iceberg.aws.glue.GlueCatalog");
    properties.put(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO");
    return properties;
  }
}
