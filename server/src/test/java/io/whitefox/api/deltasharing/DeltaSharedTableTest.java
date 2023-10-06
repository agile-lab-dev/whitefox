package io.whitefox.api.deltasharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.wildfly.common.Assert.assertTrue;

import io.whitefox.persistence.memory.PTable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

public class DeltaSharedTableTest {

  private static final Path deltaTablesRoot = Paths.get(".")
      .toAbsolutePath()
      .resolve("src/test/resources/delta/samples")
      .toAbsolutePath();

  private static String tablePath(String tableName) {
    return deltaTablesRoot.resolve(tableName).toUri().toString();
  }

  @Test
  void getTableVersion() throws ExecutionException, InterruptedException {
    var PTable = new PTable("delta-table", tablePath("delta-table"), "default", "share1");
    var DTable = new DeltaSharedTable(PTable);
    var version = DTable.getTableVersion(Optional.empty());
    assertEquals(0, version.toCompletableFuture().get());
  }

  @Test
  void getTableVersionNonExistingTable() throws ExecutionException, InterruptedException {
    var PTable =
        new PTable("delta-table", tablePath("delta-table-not-exists"), "default", "share1");
    var DTable = new DeltaSharedTable(PTable);
    var version = DTable.getTableVersion(Optional.empty());
    assertEquals(-1, version.toCompletableFuture().get());
  }

  @Test
  void getTableVersionWithTimestamp() {
    var PTable = new PTable("delta-table", tablePath("delta-table"), "default", "share1");
    var DTable = new DeltaSharedTable(PTable);
    var version = DTable.getTableVersion(Optional.of("2023-09-30T10:15:30+01:00"));
    Exception e = assertThrows(
        ExecutionException.class, () -> version.toCompletableFuture().get());
    assertTrue(e.getCause() instanceof RuntimeException);
  }

  @Test
  void getTableVersionWithBadTimestamp() {
    var PTable = new PTable("delta-table", tablePath("delta-table"), "default", "share1");
    var DTable = new DeltaSharedTable(PTable);
    var version = DTable.getTableVersion(Optional.of("2024-10-20T10:15:30+01:00"));
    Exception e = assertThrows(
        ExecutionException.class, () -> version.toCompletableFuture().get());
    assertTrue(e.getCause() instanceof RuntimeException);
  }
}
