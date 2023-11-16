package io.whitefox.core.services;

import io.whitefox.annotations.SkipCoverageGenerated;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeltaSharingCapabilities {
  private static final Logger log = LoggerFactory.getLogger(DeltaSharingCapabilities.class);
  private final Map<String, Set<String>> values;

  public Map<String, Set<String>> values() {
    return values;
  }

  public static final String DELTA_SHARE_CAPABILITIES_HEADER = "delta-sharing-capabilities";

  public DeltaSharingCapabilities(Map<String, Set<String>> values) {
    this.values = Collections.unmodifiableMap(values);
  }

  public static DeltaSharingCapabilities defaultValue() {
    return new DeltaSharingCapabilities((String) null);
  }

  public DeltaSharingCapabilities(String s) {
    this(parseDeltaSharingCapabilities(s));
  }

  public DeltaSharingCapabilities withResponseFormat(
      DeltaSharingCapabilities.DeltaSharingResponseFormat format) {
    return new DeltaSharingCapabilities(Stream.concat(
            values.entrySet().stream(),
            Stream.of(Map.entry(DELTA_SHARING_RESPONSE_FORMAT, Set.of(format.toString()))))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  private static Map<String, Set<String>> parseDeltaSharingCapabilities(String s) {
    if (s == null) {
      return Map.of();
    } else {
      return Arrays.stream(s.split(";", -1))
          .flatMap(entry -> {
            if (StringUtils.isBlank(entry)) {
              return Stream.empty();
            }
            var keyAndValues = entry.split("=", -1);
            if (keyAndValues.length != 2) {
              throw new IllegalArgumentException(String.format(
                  "Each %s must be in the format key=value", DELTA_SHARE_CAPABILITIES_HEADER));
            }
            var key = keyAndValues[0];
            var values = Arrays.stream(keyAndValues[1].split(",", -1))
                .collect(Collectors.toUnmodifiableSet());
            return Stream.of(Map.entry(key, values));
          })
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
  }

  public Set<DeltaSharingResponseFormat> getResponseFormat() {
    var value = values.get(DELTA_SHARING_RESPONSE_FORMAT);
    if (value == null || value.isEmpty()) {
      return Set.of(DeltaSharingResponseFormat.PARQUET);
    } else {
      return value.stream()
          .flatMap(s -> {
            try {
              return Stream.of(DeltaSharingResponseFormat.valueOf(s.toUpperCase()));
            } catch (IllegalArgumentException e) {
              log.warn("Ignoring unknown {}: {}", DELTA_SHARING_RESPONSE_FORMAT, s);
              return Stream.empty();
            }
          })
          .collect(Collectors.toUnmodifiableSet());
    }
  }

  public Set<DeltaSharingFeatures> getReaderFeatures() {
    var value = values.get(DELTA_SHARING_READER_FEATURES);
    if (value == null || value.isEmpty()) {
      return Set.of();
    } else {
      return value.stream()
          .flatMap(s -> {
            try {
              return Stream.of(DeltaSharingFeatures.fromString(s));
            } catch (IllegalArgumentException e) {
              log.warn("Ignoring unknown {}: {}", DELTA_SHARING_READER_FEATURES, s);
              return Stream.empty();
            }
          })
          .collect(Collectors.toSet());
    }
  }

  @Override
  @SkipCoverageGenerated
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DeltaSharingCapabilities that = (DeltaSharingCapabilities) o;
    return Objects.equals(values, that.values);
  }

  @Override
  @SkipCoverageGenerated
  public int hashCode() {
    return Objects.hash(values);
  }

  @Override
  @SkipCoverageGenerated
  public String toString() {
    return "DeltaSharingCapabilities{" + "values=" + values + '}';
  }

  public static final String DELTA_SHARING_RESPONSE_FORMAT = "responseformat";

  public enum DeltaSharingResponseFormat {
    DELTA,
    PARQUET
  }

  public static final String DELTA_SHARING_READER_FEATURES = "readerfeatures";
  static final String DELTA_SHARING_READER_FEATURE_DELETION_VECTOR = "deletionvectors";
  static final String DELTA_SHARING_READER_FEATURE_COLUMN_MAPPING = "columnmapping";
  static final String DELTA_SHARING_READER_FEATURE_TIMESTAMP_NTZ = "timestampntz";
  static final String DELTA_SHARING_READER_FEATURE_DOMAIN_METADATA = "domainmetadata";
  static final String DELTA_SHARING_READER_FEATURE_V2CHECKPOINT = "v2checkpoint";
  static final String DELTA_SHARING_READER_FEATURE_CHECK_CONSTRAINTS = "checkconstraints";
  static final String DELTA_SHARING_READER_FEATURE_GENERATED_COLUMNS = "generatedcolumns";
  static final String DELTA_SHARING_READER_FEATURE_ALLOW_COLUMN_DEFAULTS = "allowcolumndefaults";
  static final String DELTA_SHARING_READER_FEATURE_IDENTITY_COLUMNS = "identitycolumns";

  public enum DeltaSharingFeatures {
    DELETION_VECTORS(DELTA_SHARING_READER_FEATURE_DELETION_VECTOR),
    COLUMN_MAPPING(DELTA_SHARING_READER_FEATURE_COLUMN_MAPPING),
    TIMESTAMP_NTZ(DELTA_SHARING_READER_FEATURE_TIMESTAMP_NTZ),
    DOMAIN_METADATA(DELTA_SHARING_READER_FEATURE_DOMAIN_METADATA),
    V2CHECKPOINT(DELTA_SHARING_READER_FEATURE_V2CHECKPOINT),
    CHECK_CONSTRAINTS(DELTA_SHARING_READER_FEATURE_CHECK_CONSTRAINTS),
    GENERATED_COLUMNS(DELTA_SHARING_READER_FEATURE_GENERATED_COLUMNS),
    ALLOW_COLUMN_DEFAULTS(DELTA_SHARING_READER_FEATURE_ALLOW_COLUMN_DEFAULTS),
    IDENTITY_COLUMNS(DELTA_SHARING_READER_FEATURE_IDENTITY_COLUMNS);

    DeltaSharingFeatures(String stringRepresentation) {
      this.stringRepresentation = stringRepresentation;
    }

    private final String stringRepresentation;

    public String stringRepresentation() {
      return stringRepresentation;
    }

    public static DeltaSharingFeatures fromString(String s) {
      switch (s.toLowerCase()) {
        case DELTA_SHARING_READER_FEATURE_DELETION_VECTOR:
          return DeltaSharingFeatures.DELETION_VECTORS;
        case DELTA_SHARING_READER_FEATURE_COLUMN_MAPPING:
          return DeltaSharingFeatures.COLUMN_MAPPING;
        case DELTA_SHARING_READER_FEATURE_TIMESTAMP_NTZ:
          return DeltaSharingFeatures.TIMESTAMP_NTZ;
        case DELTA_SHARING_READER_FEATURE_DOMAIN_METADATA:
          return DeltaSharingFeatures.DOMAIN_METADATA;
        case DELTA_SHARING_READER_FEATURE_V2CHECKPOINT:
          return DeltaSharingFeatures.V2CHECKPOINT;
        case DELTA_SHARING_READER_FEATURE_CHECK_CONSTRAINTS:
          return DeltaSharingFeatures.CHECK_CONSTRAINTS;
        case DELTA_SHARING_READER_FEATURE_GENERATED_COLUMNS:
          return DeltaSharingFeatures.GENERATED_COLUMNS;
        case DELTA_SHARING_READER_FEATURE_ALLOW_COLUMN_DEFAULTS:
          return DeltaSharingFeatures.ALLOW_COLUMN_DEFAULTS;
        case DELTA_SHARING_READER_FEATURE_IDENTITY_COLUMNS:
          return DeltaSharingFeatures.IDENTITY_COLUMNS;
        default:
          throw new IllegalArgumentException("Unknown reader feature: " + s);
      }
    }
  }
}
