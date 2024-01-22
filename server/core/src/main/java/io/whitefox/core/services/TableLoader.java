package io.whitefox.core.services;

import io.whitefox.core.SharedTable;

public interface TableLoader {

  AbstractSharedTable loadTable(SharedTable sharedTable);
}
