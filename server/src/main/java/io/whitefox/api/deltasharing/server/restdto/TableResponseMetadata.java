package io.whitefox.api.deltasharing.server.restdto;

import io.whitefox.api.deltasharing.model.generated.MetadataResponse;
import io.whitefox.api.deltasharing.model.generated.ProtocolResponse;

public class TableResponseMetadata {

  ProtocolResponse protocol;
  MetadataResponse metadata;

  public TableResponseMetadata(ProtocolResponse protocol, MetadataResponse metadata) {
    this.protocol = protocol;
    this.metadata = metadata;
  }

  public TableResponseMetadata() {}

  public void setMetadata(MetadataResponse metadata) {
    this.metadata = metadata;
  }

  public void setProtocol(ProtocolResponse protocol) {
    this.protocol = protocol;
  }

  public MetadataResponse getMetadata() {
    return metadata;
  }

  public ProtocolResponse getProtocol() {
    return protocol;
  }
}
