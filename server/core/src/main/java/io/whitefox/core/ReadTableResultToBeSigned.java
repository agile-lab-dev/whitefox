package io.whitefox.core;

import io.whitefox.core.services.capabilities.ResponseFormat;
import java.util.List;
import lombok.Value;

@Value
public class ReadTableResultToBeSigned {
  Protocol protocol;
  Metadata metadata;
  List<TableFileToBeSigned> filesToBeSigned;
  long version;
  ResponseFormat responseFormat;
}
