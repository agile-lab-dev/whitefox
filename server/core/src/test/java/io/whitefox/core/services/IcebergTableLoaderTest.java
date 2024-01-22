package io.whitefox.core.services;

import static io.whitefox.IcebergTestUtils.icebergTableWithHadoopCatalog;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.whitefox.core.SharedTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class IcebergTableLoaderTest {

  private final IcebergTableLoader icebergTableLoader = new IcebergTableLoader();

  @Test
  @DisabledOnOs(OS.WINDOWS)
  public void loadTable() {
    SharedTable sharedTable = new SharedTable(
        "icebergtable1",
        "schema",
        "share",
        icebergTableWithHadoopCatalog("test_db", "icebergtable1"));
    assertDoesNotThrow(() -> icebergTableLoader.loadTable(sharedTable));
    // TODO: add asserts here when IcebergSharedTable.getTableVersion has been implemented
    //  assertTrue(icebergSharedTable.getTableVersion(Optional.empty()).isPresent());
    //  assertEquals(0, icebergSharedTable.getTableVersion(Optional.empty()).get());
  }

  @Test
  public void loadUnknownTable() {
    SharedTable sharedTable = new SharedTable(
        "not-found",
        "schema",
        "share",
        icebergTableWithHadoopCatalog("not-found-db", "not-found-table"));
    assertThrows(IllegalArgumentException.class, () -> icebergTableLoader.loadTable(sharedTable));
  }
}
