package io.whitefox.core.results;

import io.whitefox.core.delta.signed.FileAction;
import io.whitefox.core.delta.Metadata;
import io.whitefox.core.delta.Protocol;

import java.util.List;

public interface ReadTableResult {
  Metadata getMetadata();
  Protocol getProtocol();
  List<FileAction> getFiles();
}
