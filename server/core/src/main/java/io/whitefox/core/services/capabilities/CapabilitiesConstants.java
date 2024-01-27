package io.whitefox.core.services.capabilities;

// here because otherwise I could use it in TableReaderFeatures
public class CapabilitiesConstants {
  static final String DELTA_SHARING_READER_FEATURE_DELETION_VECTOR = "deletionvectors";
  static final String DELTA_SHARING_READER_FEATURE_COLUMN_MAPPING = "columnmapping";
  static final String DELTA_SHARING_READER_FEATURE_TIMESTAMP_NTZ = "timestampntz";
  static final String DELTA_SHARING_READER_FEATURE_DOMAIN_METADATA = "domainmetadata";
  static final String DELTA_SHARING_READER_FEATURE_V2CHECKPOINT = "v2checkpoint";
  static final String DELTA_SHARING_READER_FEATURE_CHECK_CONSTRAINTS = "checkconstraints";
  static final String DELTA_SHARING_READER_FEATURE_GENERATED_COLUMNS = "generatedcolumns";
  static final String DELTA_SHARING_READER_FEATURE_ALLOW_COLUMN_DEFAULTS = "allowcolumndefaults";
  static final String DELTA_SHARING_READER_FEATURE_IDENTITY_COLUMNS = "identitycolumns";
  static final String ICEBERG_V1 = "icebergv1";
  static final String ICEBERG_V2 = "icebergv2";
}
