package io.whitefox.core.delta;

import io.whitefox.annotations.SkipCoverageGenerated;
import io.whitefox.core.types.StructType;

import java.util.*;

public abstract class Metadata {
  Metadata() {
  }

  ParquetMetadata parquet(String id, Optional<String> name, Optional<String> description, ParquetFormat format, StructType schema, List<String> partitionColumns, Map<String, String> configuration, Optional<Long> version, Optional<Long> size, Optional<Long> numFiles) {
    return new ParquetMetadata(id, name, description, format, schema, partitionColumns, configuration, version, size, numFiles);
  }

  DeltaMetadata delta(Optional<Long> version, Optional<Long> size, Optional<Long> numFiles, String id, Optional<String> name, Optional<String> description, DeltaFormat format, StructType schema, List<String> partitionColumns, Optional<Long> createdTime, Map<String, String> configuration) {
    return new DeltaMetadata(version, size, numFiles, id, name, description, format, schema, partitionColumns, createdTime, configuration);
  }

  public static class DeltaFormat {
    private final String provider;
    private final Map<String, String> options;

    public DeltaFormat(String provider, Map<String, String> options) {
      this.provider = provider;
      this.options = Collections.unmodifiableMap(options);
    }

    public String provider() {
      return provider;
    }

    public Map<String, String> options() {
      return options;
    }

    @Override
    @SkipCoverageGenerated
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DeltaFormat that = (DeltaFormat) o;
      return Objects.equals(provider, that.provider) && Objects.equals(options, that.options);
    }

    @Override
    @SkipCoverageGenerated
    public int hashCode() {
      return Objects.hash(provider, options);
    }

    @Override
    @SkipCoverageGenerated
    public String toString() {
      return "DeltaFormat{" +
              "provider='" + provider + '\'' +
              ", options=" + options +
              '}';
    }
  }

  public static class DeltaMetadata extends Metadata {
    private final Optional<Long> version;
    private final Optional<Long> size;
    private final Optional<Long> numFiles;
    private final String id;
    private final Optional<String> name;
    private final Optional<String> description;
    private final DeltaFormat format;
    private final StructType schema;
    private final List<String> partitionColumns;
    private final Optional<Long> createdTime;
    private final Map<String, String> configuration;

    DeltaMetadata(Optional<Long> version, Optional<Long> size, Optional<Long> numFiles, String id, Optional<String> name, Optional<String> description, DeltaFormat format, StructType schema, List<String> partitionColumns, Optional<Long> createdTime, Map<String, String> configuration) {
      this.version = version;
      this.size = size;
      this.numFiles = numFiles;
      this.id = id;
      this.name = name;
      this.description = description;
      this.format = format;
      this.schema = schema;
      this.partitionColumns = partitionColumns;
      this.createdTime = createdTime;
      this.configuration = configuration;
    }

    public Optional<Long> version() {
      return version;
    }

    public Optional<Long> size() {
      return size;
    }

    public Optional<Long> numFiles() {
      return numFiles;
    }

    public String id() {
      return id;
    }

    public Optional<String> name() {
      return name;
    }

    public Optional<String> description() {
      return description;
    }

    public DeltaFormat format() {
      return format;
    }

    public StructType schema() {
      return schema;
    }

    public List<String> partitionColumns() {
      return partitionColumns;
    }

    public Optional<Long> createdTime() {
      return createdTime;
    }

    public Map<String, String> configuration() {
      return configuration;
    }

    @Override
    @SkipCoverageGenerated
    public String toString() {
      return "DeltaMetadata{" +
              "version=" + version +
              ", size=" + size +
              ", numFiles=" + numFiles +
              ", id='" + id + '\'' +
              ", name=" + name +
              ", description=" + description +
              ", format=" + format +
              ", schema=" + schema +
              ", partitionColumns=" + partitionColumns +
              ", createdTime=" + createdTime +
              ", configuration=" + configuration +
              '}';
    }

    @Override
    @SkipCoverageGenerated
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DeltaMetadata that = (DeltaMetadata) o;
      return Objects.equals(version, that.version) && Objects.equals(size, that.size) && Objects.equals(numFiles, that.numFiles) && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(format, that.format) && Objects.equals(schema, that.schema) && Objects.equals(partitionColumns, that.partitionColumns) && Objects.equals(createdTime, that.createdTime) && Objects.equals(configuration, that.configuration);
    }

    @Override
    @SkipCoverageGenerated
    public int hashCode() {
      return Objects.hash(version, size, numFiles, id, name, description, format, schema, partitionColumns, createdTime, configuration);
    }
  }

  public static class ParquetMetadata extends Metadata {
    private final String id;
    private final Optional<String> name;
    private final Optional<String> description;
    private final ParquetFormat format;
    private final StructType schema;
    private final List<String> partitionColumns;
    private final Map<String, String> configuration;
    private final Optional<Long> version;
    private final Optional<Long> size;
    private final Optional<Long> numFiles;

    ParquetMetadata(String id, Optional<String> name, Optional<String> description, ParquetFormat format, StructType schema, List<String> partitionColumns, Map<String, String> configuration, Optional<Long> version, Optional<Long> size, Optional<Long> numFiles) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.format = format;
      this.schema = schema;
      this.partitionColumns = partitionColumns;
      this.configuration = configuration;
      this.version = version;
      this.size = size;
      this.numFiles = numFiles;
      HashSet<String> partitionColumnSet = new HashSet<>(schema.fieldNames());
      for (String partitionColumn : partitionColumns) {
        if (!partitionColumnSet.contains(partitionColumn)){
          throw new IllegalArgumentException(
                  String.format("Partition column %s is not part of schema %s", partitionColumn, schema)
          );
        }
      }
    }

    public String id() {
      return id;
    }

    public Optional<String> name() {
      return name;
    }

    public Optional<String> description() {
      return description;
    }

    public ParquetFormat format() {
      return format;
    }

    public StructType schema() {
      return schema;
    }

    public List<String> partitionColumns() {
      return partitionColumns;
    }

    public Map<String, String> configuration() {
      return configuration;
    }

    public Optional<Long> version() {
      return version;
    }

    public Optional<Long> size() {
      return size;
    }

    public Optional<Long> numFiles() {
      return numFiles;
    }

    @Override
    @SkipCoverageGenerated
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ParquetMetadata that = (ParquetMetadata) o;
      return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && format == that.format && Objects.equals(schema, that.schema) && Objects.equals(partitionColumns, that.partitionColumns) && Objects.equals(configuration, that.configuration) && Objects.equals(version, that.version) && Objects.equals(size, that.size) && Objects.equals(numFiles, that.numFiles);
    }

    @Override
    @SkipCoverageGenerated
    public int hashCode() {
      return Objects.hash(id, name, description, format, schema, partitionColumns, configuration, version, size, numFiles);
    }

    @Override
    @SkipCoverageGenerated
    public String toString() {
      return "ParquetMetadata{" +
              "id='" + id + '\'' +
              ", name=" + name +
              ", description=" + description +
              ", format=" + format +
              ", schema=" + schema +
              ", partitionColumns=" + partitionColumns +
              ", configuration=" + configuration +
              ", version=" + version +
              ", size=" + size +
              ", numFiles=" + numFiles +
              '}';
    }
  }

  public enum ParquetFormat {PARQUET_FORMAT}
}
