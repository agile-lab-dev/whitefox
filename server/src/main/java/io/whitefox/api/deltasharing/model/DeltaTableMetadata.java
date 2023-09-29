package io.whitefox.api.deltasharing.model;

import io.delta.standalone.actions.Metadata;

public class DeltaTableMetadata {
  long tableVersion;
  Metadata metadata;

  public DeltaTableMetadata(long tableVersion, Metadata metadata) {
    this.tableVersion = tableVersion;
    this.metadata = metadata;
  }

  public long getTableVersion() {
    return tableVersion;
  }

  public Metadata getMetadata() {
    return metadata;
  }
}
