package io.whitefox.core.services;

import io.whitefox.core.Table;
import jakarta.inject.Singleton;

@Singleton
public class DeltaShareTableLoader implements TableLoader<DeltaSharedTable> {

  @Override
  public DeltaSharedTable loadTable(Table table) {
    return DeltaSharedTable.of(table);
  }
}
