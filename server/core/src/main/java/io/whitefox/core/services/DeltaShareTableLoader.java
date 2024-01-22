package io.whitefox.core.services;

import io.whitefox.core.SharedTable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeltaShareTableLoader implements TableLoader {

  @Override
  public DeltaSharedTable loadTable(SharedTable sharedTable) {
    return DeltaSharedTable.of(sharedTable);
  }
}
