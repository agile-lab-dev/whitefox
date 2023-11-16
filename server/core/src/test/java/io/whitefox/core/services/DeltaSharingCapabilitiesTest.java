package io.whitefox.core.services;

import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeltaSharingCapabilitiesTest {

  @Test
  void parseSimpleResponseFormatDelta() {
    Assertions.assertEquals(
        Set.of(DeltaSharingCapabilities.DeltaSharingResponseFormat.DELTA),
        new DeltaSharingCapabilities("responseformat=delta").getResponseFormat());
  }

  @Test
  void parseSimpleResponseFormatParquet() {
    Assertions.assertEquals(
        Set.of(DeltaSharingCapabilities.DeltaSharingResponseFormat.PARQUET),
        new DeltaSharingCapabilities("responseformat=PaRquEt").getResponseFormat());
  }

  @Test
  void failToParseUnknownResponseFormatAndReturnNothing() {
    Assertions.assertEquals(
        Set.of(), new DeltaSharingCapabilities("responseformat=iceberg").getResponseFormat());
  }

  @Test
  void failToParseUnknownResponseFormatAndReturnOthers() {
    Assertions.assertEquals(
        Set.of(
            DeltaSharingCapabilities.DeltaSharingResponseFormat.DELTA,
            DeltaSharingCapabilities.DeltaSharingResponseFormat.PARQUET),
        new DeltaSharingCapabilities("responseformat=iceberg,parquet,delta").getResponseFormat());
  }

  @Test
  void noCapabilitiesEqualsDefault() {
    Assertions.assertEquals(
        Set.of(DeltaSharingCapabilities.DeltaSharingResponseFormat.PARQUET),
        new DeltaSharingCapabilities((String) null).getResponseFormat());
    Assertions.assertEquals(
        Set.of(), new DeltaSharingCapabilities((String) null).getReaderFeatures());
    Assertions.assertEquals(
        Set.of(DeltaSharingCapabilities.DeltaSharingResponseFormat.PARQUET),
        new DeltaSharingCapabilities("").getResponseFormat());
    Assertions.assertEquals(Set.of(), new DeltaSharingCapabilities("").getReaderFeatures());
  }

  @Test
  void parseSimpleReaderFeature() {
    Assertions.assertEquals(
        Set.of(DeltaSharingCapabilities.DeltaSharingFeatures.DELETION_VECTORS),
        new DeltaSharingCapabilities(String.format(
                "%s=%s",
                DeltaSharingCapabilities.DELTA_SHARING_READER_FEATURES,
                DeltaSharingCapabilities.DELTA_SHARING_READER_FEATURE_DELETION_VECTOR))
            .getReaderFeatures());
  }

  @Test
  void failToParseUnknownReaderFeatureAndReturnNothing() {
    Assertions.assertEquals(
        Set.of(),
        new DeltaSharingCapabilities(String.format(
                "%s=%s", DeltaSharingCapabilities.DELTA_SHARING_READER_FEATURES, "unknown"))
            .getReaderFeatures());
  }

  @Test
  void failToParseUnknownReaderFeatureAndReturnOthers() {
    Assertions.assertEquals(
        Set.of(
            DeltaSharingCapabilities.DeltaSharingFeatures.COLUMN_MAPPING,
            DeltaSharingCapabilities.DeltaSharingFeatures.DOMAIN_METADATA),
        new DeltaSharingCapabilities(String.format(
                "%s=%s,%s,%s",
                DeltaSharingCapabilities.DELTA_SHARING_READER_FEATURES,
                "unknown",
                DeltaSharingCapabilities.DELTA_SHARING_READER_FEATURE_COLUMN_MAPPING,
                DeltaSharingCapabilities.DELTA_SHARING_READER_FEATURE_DOMAIN_METADATA))
            .getReaderFeatures());
  }

  @Test
  void kitchenSink() {
    var readerFeatures = String.format(
        "%s=%s,%s,%s",
        DeltaSharingCapabilities.DELTA_SHARING_READER_FEATURES,
        "unknown",
        DeltaSharingCapabilities.DELTA_SHARING_READER_FEATURE_COLUMN_MAPPING,
        DeltaSharingCapabilities.DELTA_SHARING_READER_FEATURE_DOMAIN_METADATA);
    var responseFormat = "responseformat=iceberg,parquet,delta";
    var randomStuff = "thisIsAKey=these,some,values,lol";
    var capabilities = new DeltaSharingCapabilities(
        String.format("%s;%s;%s", readerFeatures, responseFormat, randomStuff));
    Assertions.assertEquals(
        Set.of(
            DeltaSharingCapabilities.DeltaSharingResponseFormat.DELTA,
            DeltaSharingCapabilities.DeltaSharingResponseFormat.PARQUET),
        capabilities.getResponseFormat());
    Assertions.assertEquals(
        Set.of(
            DeltaSharingCapabilities.DeltaSharingFeatures.COLUMN_MAPPING,
            DeltaSharingCapabilities.DeltaSharingFeatures.DOMAIN_METADATA),
        capabilities.getReaderFeatures());
    Assertions.assertEquals(
        Set.of("these", "some", "values", "lol"), capabilities.values().get("thisIsAKey"));
  }
}
