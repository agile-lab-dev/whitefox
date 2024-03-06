package io.whitefox.core.services;

import io.whitefox.core.Metadata;
import io.whitefox.core.services.capabilities.ResponseFormat;
import lombok.Value;

@Value
public class MetadataResponse {
  Metadata metadata;
  ResponseFormat responseFormat;
}
