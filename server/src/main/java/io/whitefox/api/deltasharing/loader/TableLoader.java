package io.whitefox.api.deltasharing.loader;

import io.whitefox.persistence.memory.PTable;

public interface TableLoader<T> {

  T loadTable(PTable pTable);
}
