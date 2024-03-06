package io.whitefox.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Value;

@Value
public class Metadata {
  String id;
  Optional<String> name;
  Optional<String> description;
  FileFormat format;
  TableSchema tableSchema;
  List<String> partitionColumns;
  Map<String, String> configuration;
  Optional<Long> version;
  Optional<Long> size;
  Optional<Long> numFiles;
}
