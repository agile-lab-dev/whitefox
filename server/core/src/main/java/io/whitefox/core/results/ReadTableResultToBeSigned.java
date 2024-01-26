package io.whitefox.core.results;

import io.whitefox.core.delta.Metadata;
import io.whitefox.core.delta.Protocol;
import io.whitefox.core.delta.unsigned.FileActionToBeSigned;

import java.util.List;

public interface ReadTableResultToBeSigned {
  Metadata getMetadata();

  Protocol getProtocol();

  List<FileActionToBeSigned> getFiles();
}
