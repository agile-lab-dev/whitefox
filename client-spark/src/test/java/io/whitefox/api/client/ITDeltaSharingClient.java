package io.whitefox.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.mrpowers.spark.fast.tests.DatasetComparer;
import io.whitefox.api.client.model.TableInfo;
import io.whitefox.api.models.MrFoxDeltaTableSchema;
import io.whitefox.api.utils.ScalaUtils;
import io.whitefox.api.utils.StorageManagerInitializer;
import io.whitefox.api.utils.TablePath;
import io.whitefox.api.utils.TestSparkSession;
import java.util.List;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("clientSparkTest")
public class ITDeltaSharingClient implements DatasetComparer, ScalaUtils {

  private final StorageManagerInitializer storageManagerInitializer;
  private final String deltaTablePath;
  private final String icebergTablePath;

  public ITDeltaSharingClient() {
    this.storageManagerInitializer = new StorageManagerInitializer();
    this.deltaTablePath =
        TablePath.getDeltaTablePath(getClass().getClassLoader().getResource("MrFoxProfile.json"));
    this.icebergTablePath =
        TablePath.getIcebergTablePath(getClass().getClassLoader().getResource("MrFoxProfile.json"));
  }

  @BeforeAll
  static void initStorageManager() {
    new StorageManagerInitializer().initStorageManager();
  }

  @Test
  void showS3IcebergTable1withQueryTableApi() {
    var spark = TestSparkSession.newSparkSession();
    storageManagerInitializer.createIcebergTableWithGlueMetastore();
    var ds = spark.read().format("deltaSharing").load(icebergTablePath);
    var expectedSchema = new StructType(new StructField[] {
      new StructField("id", DataType.fromDDL("long"), false, new Metadata(emptyScalaMap()))
    });
    var expectedData = spark
        .createDataFrame(
            List.of(
                new MrFoxDeltaTableSchema(0),
                new MrFoxDeltaTableSchema(3),
                new MrFoxDeltaTableSchema(2),
                new MrFoxDeltaTableSchema(1),
                new MrFoxDeltaTableSchema(4)),
            MrFoxDeltaTableSchema.class)
        .toDF();

    assertEquals(expectedSchema, ds.schema());
    assertEquals(5, ds.count());
    assertSmallDatasetEquality(ds, expectedData, true, false, false, 500);
  }

  @Test
  void showS3Table1withQueryTableApi() {
    var spark = TestSparkSession.newSparkSession();
    storageManagerInitializer.createS3DeltaTable();
    var ds = spark.read().format("deltaSharing").load(deltaTablePath);
    var expectedSchema = new StructType(new StructField[] {
      new StructField("id", DataType.fromDDL("long"), true, new Metadata(emptyScalaMap()))
    });

    ds.show();
    var expectedData = spark
        .createDataFrame(
            List.of(
                new MrFoxDeltaTableSchema(0),
                new MrFoxDeltaTableSchema(3),
                new MrFoxDeltaTableSchema(2),
                new MrFoxDeltaTableSchema(1),
                new MrFoxDeltaTableSchema(4)),
            MrFoxDeltaTableSchema.class)
        .toDF();

    assertEquals(expectedSchema, ds.schema());
    assertEquals(5, ds.count());
    assertSmallDatasetEquality(ds, expectedData, true, false, false, 500);
  }

  @Test
  void registerAnIcebergTable() {
    TableInfo tableInfo = storageManagerInitializer.createIcebergTableWithGlueMetastore();
    assertEquals(tableInfo.getName(), "s3IcebergTable1");
  }
}
