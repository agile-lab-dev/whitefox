package io.whitefox.core.services;

import io.whitefox.core.SharedTable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IcebergTableLoader implements TableLoader {

  @Override
  public IcebergSharedTable loadTable(SharedTable sharedTable) {
    return IcebergSharedTable.of(sharedTable);
  }
}
