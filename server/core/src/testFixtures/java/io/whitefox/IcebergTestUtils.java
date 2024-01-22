package io.whitefox;

import io.whitefox.core.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class IcebergTestUtils extends TestUtils {

  private static final Path icebergTablesRoot = Paths.get(".")
      .toAbsolutePath()
      .getParent()
      .getParent()
      .resolve("core")
      .resolve("src/testFixtures/resources/iceberg/samples")
      .toAbsolutePath();

  public static String icebergTableUri(String tableName) {
    return icebergTablesRoot
        .resolve(tableName)
        .toAbsolutePath()
        .normalize()
        .toUri()
        .toString();
  }

  public static InternalTable icebergTableWithHadoopCatalog(String database, String tableName) {
    var mrFoxPrincipal = new Principal("Mr. Fox");
    return new InternalTable(
        tableName,
        Optional.empty(),
        new InternalTable.IcebergTableProperties(database, tableName),
        Optional.of(0L),
        0L,
        mrFoxPrincipal,
        0L,
        mrFoxPrincipal,
        getProvider(
            getLocalStorage(mrFoxPrincipal),
            mrFoxPrincipal,
            Optional.of(getLocalHadoopMetastore(mrFoxPrincipal, icebergTablesRoot.toString()))));
  }

  public static Metastore getLocalHadoopMetastore(Principal principal, String location) {
    return getMetastore(
        principal,
        MetastoreType.HADOOP,
        new MetastoreProperties.HadoopMetastoreProperties(location, MetastoreType.HADOOP));
  }
}
