package io.whitefox.core.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import io.whitefox.IcebergTestUtils;
import java.io.IOException;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;

@DisabledOnOs(WINDOWS)
public class IcebergCatalogServiceTest {

  /**
   * This is some sample code that you need to run in your spark shell to generate new iceberg tables for new test cases:
   * To run the spark-shell with delta support execute:
   * {{{
   * spark-shell --packages org.apache.iceberg:iceberg-spark-runtime-3.5_2.12:1.4.2,org.apache.iceberg:iceberg-aws-bundle:1.4.2 \
   *         										--conf spark.sql.catalog.spark_catalog=org.apache.iceberg.spark.SparkCatalog \
   *         										--conf spark.sql.catalog.spark_catalog.type=hadoop \
   *         										--conf spark.sql.catalog.spark_catalog.warehouse=/Volumes/repos/oss/whitefox/server/core/src/testFixtures/resources/iceberg/samples/ \
   *                                                --conf spark.sql.extensions=org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions
   * Take care that the version of iceberg must be compatible with the version of spark and scala you are using
   * (i.e. I'm using iceberg 3.5 on scala 2.12 because my local spark-shell is version 3.5.0 using scala 2.12
   *
   * First, uou need to create an iceberg table with your local hadoop catalog
   * {{{
   * 		import org.apache.iceberg.catalog.Namespace
   * 		import org.apache.iceberg.Schema
   * 		import org.apache.iceberg.catalog.TableIdentifier
   * 		import org.apache.iceberg.hadoop.HadoopCatalog
   * 		import java.util.Map
   * 		import org.apache.hadoop.conf.Configuration
   *
   *  		val catalog = new HadoopCatalog()
   *     	catalog.setConf(new Configuration())
   *     	catalog.initialize("test_hadoop_catalog",
   *       	    Map.of("warehouse", "/Volumes/repos/oss/whitefox/server/core/src/testFixtures/resources/iceberg/samples/"))
   *        catalog.createNamespace(Namespace.of("test_db"))
   *        val schema = new Schema(org.apache.iceberg.types.Types.NestedField.required(1, "id", org.apache.iceberg.types.Types.LongType.get()))
   * 		catalog.createTable(TableIdentifier.of("test_db", "icebergtable1"),  schema)
   * }}}
   *
   * Then, you can append data on your iceberg table
   * {{{
   * 		val data = spark.range(0, 5)
   * 		data.writeTo("test_db.icebergtable1").append()
   * }}}
   */
  @Test
  void simpleIcebergTest() throws IOException {
    try (HadoopCatalog hadoopCatalog = new HadoopCatalog()) {
      // Initialize catalog
      hadoopCatalog.setConf(new Configuration());
      hadoopCatalog.initialize(
          "test_hadoop_catalog",
          Map.of("warehouse", IcebergTestUtils.icebergTablesRoot.toString()));
      TableIdentifier tableIdentifier = TableIdentifier.of("test_db", "icebergtable1");

      // Load the Iceberg table
      Table table = hadoopCatalog.loadTable(tableIdentifier);
      assertEquals("test_hadoop_catalog.test_db.icebergtable1", table.name());
    }
  }
}
