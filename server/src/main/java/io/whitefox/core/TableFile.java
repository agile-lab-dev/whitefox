package io.whitefox.core;

import io.whitefox.annotations.SkipCoverageGenerated;
import java.util.Map;
import java.util.Objects;

public class TableFile {

  private final String url;
  private final String id;
  private final long size;

  private final Map<String, String> partitionValues;

  private final long expirationTimestamp;

  public TableFile(
      String url,
      String id,
      long size,
      Map<String, String> partitionValues,
      long expirationTimestamp) {
    this.url = url;
    this.id = id;
    this.size = size;
    this.partitionValues = partitionValues;
    this.expirationTimestamp = expirationTimestamp;
  }

  @Override
  @SkipCoverageGenerated
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TableFile tableFile = (TableFile) o;
    return size == tableFile.size
        && expirationTimestamp == tableFile.expirationTimestamp
        && Objects.equals(url, tableFile.url)
        && Objects.equals(id, tableFile.id)
        && Objects.equals(partitionValues, tableFile.partitionValues);
  }

  @Override
  @SkipCoverageGenerated
  public int hashCode() {
    return Objects.hash(url, id, size, partitionValues, expirationTimestamp);
  }

  public String url() {
    return url;
  }

  public String id() {
    return id;
  }

  public long size() {
    return size;
  }

  public Map<String, String> partitionValues() {
    return partitionValues;
  }

  public long expirationTimestamp() {
    return expirationTimestamp;
  }

  @Override
  @SkipCoverageGenerated
  public String toString() {
    return "TableFile{" + "url='"
        + url + '\'' + ", id='"
        + id + '\'' + ", size="
        + size + ", partitionValues="
        + partitionValues + ", expirationTimestamp="
        + expirationTimestamp + '}';
  }
}
