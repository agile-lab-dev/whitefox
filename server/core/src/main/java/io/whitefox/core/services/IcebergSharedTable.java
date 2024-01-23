package io.whitefox.core.services;

import io.whitefox.core.*;
import io.whitefox.core.aws.utils.StaticCredentialsProvider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.iceberg.BaseMetastoreCatalog;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.Table;
import org.apache.iceberg.aws.AwsClientProperties;
import org.apache.iceberg.aws.AwsProperties;
import org.apache.iceberg.aws.glue.GlueCatalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.NoSuchTableException;
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
      var tableId = getTableIdentifier(tableDetails.internalTable());
      var table = loadTable(
          metastore,
          tableDetails.internalTable().provider().storage(),
          tableId,
          hadoopConfigBuilder);
      return new IcebergSharedTable(table, tableSchemaConverter);
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

  private static Table loadTable(
      Metastore metastore,
      Storage storage,
      TableIdentifier tableIdentifier,
      HadoopConfigBuilder hadoopConfigBuilder) {
    if (metastore.type() == MetastoreType.GLUE) {
      return loadTableWithGlueCatalog(metastore, storage, tableIdentifier, hadoopConfigBuilder);
    } else if (metastore.type() == MetastoreType.HADOOP) {
      return loadTableWithHadoopCatalog(metastore, storage, tableIdentifier, hadoopConfigBuilder);
    } else {
      throw new RuntimeException(String.format("Unknown metastore type: [%s]", metastore.type()));
    }
  }

  private static Table loadTableWithGlueCatalog(
      Metastore metastore,
      Storage storage,
      TableIdentifier tableIdentifier,
      HadoopConfigBuilder hadoopConfigBuilder) {
    try (var catalog = new GlueCatalog()) {
      catalog.setConf(hadoopConfigBuilder.buildConfig(storage));
      catalog.initialize(
          metastore.name(),
          setGlueProperties((MetastoreProperties.GlueMetastoreProperties) metastore.properties()));
      return loadTable(catalog, tableIdentifier);
    } catch (IOException e) {
      throw new RuntimeException("Unexpected error when closing the Glue catalog", e);
    }
  }

  private static Table loadTableWithHadoopCatalog(
      Metastore metastore,
      Storage storage,
      TableIdentifier tableIdentifier,
      HadoopConfigBuilder hadoopConfigBuilder) {
    try (var catalog = new HadoopCatalog()) {
      catalog.setConf(hadoopConfigBuilder.buildConfig(storage));
      catalog.initialize(
          metastore.name(),
          Map.of(
              CatalogProperties.WAREHOUSE_LOCATION,
              ((MetastoreProperties.HadoopMetastoreProperties) metastore.properties()).location()));
      return loadTable(catalog, tableIdentifier);
    } catch (IOException e) {
      throw new RuntimeException("Unexpected error when closing the Hadoop catalog", e);
    }
  }

  private static Table loadTable(BaseMetastoreCatalog catalog, TableIdentifier tableIdentifier) {
    try {
      return catalog.loadTable(tableIdentifier);
    } catch (NoSuchTableException e) {
      throw new IllegalArgumentException(String.format(
          "Cannot found iceberg table [%s] under namespace [%s]",
          tableIdentifier.name(), tableIdentifier.namespace()));
    } catch (Throwable e) {
      throw new RuntimeException(String.format(
          "Unexpected exception when loading the iceberg table [%s] under namespace [%s]",
          tableIdentifier.name(), tableIdentifier.namespace()));
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

  private static Map<String, String> setGlueProperties(
      MetastoreProperties.GlueMetastoreProperties glueMetastoreProperties) {
    AwsCredentials.SimpleAwsCredentials credentials =
        (AwsCredentials.SimpleAwsCredentials) glueMetastoreProperties.credentials();
    Map<String, String> properties = new HashMap<>();
    properties.put(CatalogProperties.CATALOG_IMPL, "org.apache.iceberg.aws.glue.GlueCatalog");
    properties.put(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO");
    properties.put(AwsProperties.GLUE_CATALOG_ID, glueMetastoreProperties.catalogId());
    properties.put(AwsClientProperties.CLIENT_REGION, credentials.region());
    properties.put(
        AwsClientProperties.CLIENT_CREDENTIALS_PROVIDER, StaticCredentialsProvider.class.getName());
    properties.put(
        String.format("%s.%s", AwsClientProperties.CLIENT_CREDENTIALS_PROVIDER, "accessKeyId"),
        credentials.awsAccessKeyId());
    properties.put(
        String.format("%s.%s", AwsClientProperties.CLIENT_CREDENTIALS_PROVIDER, "secretAccessKey"),
        credentials.awsSecretAccessKey());
    return properties;
  }
}
