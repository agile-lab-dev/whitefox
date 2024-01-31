package io.whitefox.core.services;

import io.whitefox.core.*;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.aws.s3.S3FileIO;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.io.FileIO;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class FileIOFactoryImpl implements FileIOFactory {

  @Override
  // TODO add tests to restore coverage from 0.70 to 0.72
  public FileIO newFileIO(Storage storage, Metastore metastore) {
    if (metastore.type() == MetastoreType.GLUE) {
      if (storage.properties() instanceof StorageProperties.S3Properties) {
        AwsCredentials credentials =
            ((StorageProperties.S3Properties) storage.properties()).credentials();
        if (credentials instanceof AwsCredentials.SimpleAwsCredentials simpleAwsCredentials) {
          var s3FileIO = new S3FileIO(() -> S3Client.builder()
              .region(Region.of(simpleAwsCredentials.region()))
              .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                  simpleAwsCredentials.awsAccessKeyId(),
                  simpleAwsCredentials.awsSecretAccessKey())))
              .build());
          s3FileIO.initialize(Map.of());
          return s3FileIO;
        } else {
          throw new RuntimeException("Missing aws credentials");
        }
      } else {
        throw new RuntimeException("Missing s3 properties");
      }
    } else if (metastore.type() == MetastoreType.HADOOP) {
      var hadoopFileIO = new HadoopFileIO(new Configuration());
      hadoopFileIO.initialize(Map.of(
          CatalogProperties.WAREHOUSE_LOCATION,
          ((MetastoreProperties.HadoopMetastoreProperties) metastore.properties()).location()));
      return hadoopFileIO;
    } else
      throw new IllegalArgumentException(
          String.format("Unsupported metastore type: [%s]", metastore.type()));
  }
}
