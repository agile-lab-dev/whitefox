package io.whitefox.core;

import io.whitefox.annotations.SkipCoverageGenerated;
import java.util.Map;
import java.util.Objects;

public class TableFileToBeSigned {

  private final String url;
  private final long size;
  private final Map<String, String> partitionValues;

  public TableFileToBeSigned(String url, long size, Map<String, String> partitionValues) {
    this.url = url;
    this.size = size;
    this.partitionValues = partitionValues;
  }

  @Override
  @SkipCoverageGenerated
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TableFileToBeSigned that = (TableFileToBeSigned) o;
    return size == that.size
        && Objects.equals(url, that.url)
        && Objects.equals(partitionValues, that.partitionValues);
  }

  @Override
  @SkipCoverageGenerated
  public int hashCode() {
    return Objects.hash(url, size, partitionValues);
  }

  @Override
  @SkipCoverageGenerated
  public String toString() {
    return "TableFileToBeSigned{" + "url='"
        + url + '\'' + ", size="
        + size + ", partitionValues="
        + partitionValues + '}';
  }

  public String url() {
    return url;
  }

  public long size() {
    return size;
  }

  public Map<String, String> partitionValues() {
    return partitionValues;
  }
}
