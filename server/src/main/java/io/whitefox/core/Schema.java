package io.whitefox.core;

import io.whitefox.annotations.SkipCoverageGenerated;
import java.util.*;

public final class Schema {
  private final String name;
  private final Set<SharedTable> sharedTables;
  private final String share;

  public Schema(String name, Collection<SharedTable> sharedTables, String share) {
    this.name = name;
    this.sharedTables = Set.copyOf(sharedTables);
    this.share = share;
  }

  public String name() {
    return name;
  }

  public Set<SharedTable> tables() {
    return sharedTables;
  }

  public String share() {
    return share;
  }

  @Override
  @SkipCoverageGenerated
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (Schema) obj;
    return Objects.equals(this.name, that.name)
        && Objects.equals(this.sharedTables, that.sharedTables)
        && Objects.equals(this.share, that.share);
  }

  @Override
  @SkipCoverageGenerated
  public int hashCode() {
    return Objects.hash(name, sharedTables, share);
  }

  @Override
  @SkipCoverageGenerated
  public String toString() {
    return "Schema[" + "name=" + name + ", " + "tables=" + sharedTables + ", " + "share=" + share
        + ']';
  }

  public Schema addTable(InternalTable table) {
    var newSharedTables = new HashSet<>(sharedTables);
    newSharedTables.add(new SharedTable(table.name(), name, share, table));
    return new Schema(name, newSharedTables, share);
  }
}
