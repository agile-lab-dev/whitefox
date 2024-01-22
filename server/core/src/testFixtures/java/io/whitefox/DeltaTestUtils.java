package io.whitefox;

import io.whitefox.core.InternalTable;
import io.whitefox.core.Principal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class DeltaTestUtils extends TestUtils {

  private static final Path deltaTablesRoot = Paths.get(".")
      .toAbsolutePath()
      .getParent()
      .getParent()
      .resolve("core")
      .resolve("src/testFixtures/resources/delta/samples")
      .toAbsolutePath();

  public static String deltaTableUri(String tableName) {
    return deltaTablesRoot
        .resolve(tableName)
        .toAbsolutePath()
        .normalize()
        .toUri()
        .toString();
  }

  public static InternalTable deltaTable(String tableName) {
    var mrFoxPrincipal = new Principal("Mr. Fox");
    return new InternalTable(
        tableName,
        Optional.empty(),
        new InternalTable.DeltaTableProperties(deltaTableUri(tableName)),
        Optional.of(0L),
        0L,
        mrFoxPrincipal,
        0L,
        mrFoxPrincipal,
        getProvider(getLocalStorage(mrFoxPrincipal), mrFoxPrincipal, Optional.empty()));
  }
}
