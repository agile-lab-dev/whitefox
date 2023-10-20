package io.whitefox.api.deltasharing.server;

import io.whitefox.annotations.SkipCoverageGenerated;
import io.whitefox.api.deltasharing.model.v1.generated.MetadataObject;
import io.whitefox.api.deltasharing.model.v1.generated.ProtocolObject;
import java.util.Objects;

public class TableResponseMetadata {

  private ProtocolObject protocol;
  private MetadataObject metadata;

  public TableResponseMetadata(ProtocolObject protocol, MetadataObject metadata) {
    this.protocol = protocol;
    this.metadata = metadata;
  }

  public TableResponseMetadata() {}

  public void setMetadata(MetadataObject metadata) {
    this.metadata = metadata;
  }

  public void setProtocol(ProtocolObject protocol) {
    this.protocol = protocol;
  }

  public MetadataObject getMetadata() {
    return metadata;
  }

  public ProtocolObject getProtocol() {
    return protocol;
  }

  @Override
  @SkipCoverageGenerated
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TableResponseMetadata that = (TableResponseMetadata) o;

    if (!Objects.equals(protocol, that.protocol)) return false;
    return Objects.equals(metadata, that.metadata);
  }

  @Override
  @SkipCoverageGenerated
  public int hashCode() {
    int result = protocol != null ? protocol.hashCode() : 0;
    result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
    return result;
  }

  @Override
  @SkipCoverageGenerated
  public String toString() {
    return "TableResponseMetadata{" + "protocol=" + protocol + ", metadata=" + metadata + '}';
  }
}
