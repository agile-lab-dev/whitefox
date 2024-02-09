package io.whitefox.api.deltasharing;

import io.whitefox.api.server.DeltaHeaders;
import io.whitefox.core.services.capabilities.CapabilitiesConstants;
import io.whitefox.core.services.capabilities.ReaderFeatures;
import io.whitefox.core.services.capabilities.ResponseFormat;
import io.whitefox.core.services.exceptions.UnknownResponseFormat;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClientCapabilitiesMapperTest implements DeltaHeaders {

  ClientCapabilitiesMapper mapper = new ClientCapabilitiesMapper();
  String responseFormatDelta = "responseformat=delta";

  @Test
  void parseSimpleResponseFormatDelta() {
    Assertions.assertEquals(
        Set.of(ResponseFormat.delta, ResponseFormat.parquet),
        mapper.parseDeltaSharingCapabilities(responseFormatDelta).responseFormats());
  }

  @Test
  void parseSimpleResponseFormatParquet() {
    Assertions.assertEquals(
        Set.of(ResponseFormat.parquet),
        mapper.parseDeltaSharingCapabilities("responseformat=PaRquEt").responseFormats());
  }

  @Test
  void failToParseUnknownResponseFormatAndFail() {
    Assertions.assertThrows(
        UnknownResponseFormat.class,
        () -> mapper.parseDeltaSharingCapabilities("responseformat=iceberg").responseFormats());
  }

  @Test
  void failToParseUnknownResponseFormatAndReturnOthers() {
    Assertions.assertEquals(
        Set.of(ResponseFormat.delta, ResponseFormat.parquet),
        mapper
            .parseDeltaSharingCapabilities("responseformat=iceberg,parquet,delta")
            .responseFormats());
  }

  @Test
  void noCapabilitiesEqualsDefault() {
    Assertions.assertEquals(
        Set.of(ResponseFormat.parquet),
        mapper.parseDeltaSharingCapabilities((String) null).responseFormats());
    Assertions.assertEquals(
        Set.of(), mapper.parseDeltaSharingCapabilities((String) null).readerFeatures());
    Assertions.assertEquals(
        Set.of(ResponseFormat.parquet), mapper.parseDeltaSharingCapabilities("").responseFormats());
    Assertions.assertEquals(Set.of(), mapper.parseDeltaSharingCapabilities("").readerFeatures());
  }

  @Test
  void parseSimpleReaderFeature() {
    Assertions.assertEquals(
        Set.of(ReaderFeatures.DELETION_VECTORS),
        mapper
            .parseDeltaSharingCapabilities(String.format(
                responseFormatDelta + ";%s=%s",
                DELTA_SHARING_READER_FEATURES,
                CapabilitiesConstants.DELTA_SHARING_READER_FEATURE_DELETION_VECTOR))
            .readerFeatures());
  }

  @Test
  void failToParseUnknownReaderFeatureAndReturnNothing() {
    Assertions.assertEquals(
        Set.of(),
        mapper
            .parseDeltaSharingCapabilities(
                String.format("%s=%s", DELTA_SHARING_READER_FEATURES, "unknown"))
            .readerFeatures());
  }

  @Test
  void failToParseUnknownReaderFeatureAndReturnOthers() {
    Assertions.assertEquals(
        Set.of(ReaderFeatures.COLUMN_MAPPING, ReaderFeatures.DOMAIN_METADATA),
        mapper
            .parseDeltaSharingCapabilities(String.format(
                responseFormatDelta + ";%s=%s,%s,%s",
                DELTA_SHARING_READER_FEATURES,
                "unknown",
                CapabilitiesConstants.DELTA_SHARING_READER_FEATURE_COLUMN_MAPPING,
                CapabilitiesConstants.DELTA_SHARING_READER_FEATURE_DOMAIN_METADATA))
            .readerFeatures());
  }

  @Test
  void kitchenSink() {
    var readerFeatures = String.format(
        "%s=%s,%s,%s",
        DELTA_SHARING_READER_FEATURES,
        "unknown",
        CapabilitiesConstants.DELTA_SHARING_READER_FEATURE_COLUMN_MAPPING,
        CapabilitiesConstants.DELTA_SHARING_READER_FEATURE_DOMAIN_METADATA);
    var responseFormat = "responseformat=iceberg,parquet,delta";
    var capabilities = mapper.parseDeltaSharingCapabilities(
        String.format("%s;%s", readerFeatures, responseFormat));
    Assertions.assertEquals(
        Set.of(ResponseFormat.delta, ResponseFormat.parquet), capabilities.responseFormats());
    Assertions.assertEquals(
        Set.of(ReaderFeatures.COLUMN_MAPPING, ReaderFeatures.DOMAIN_METADATA),
        capabilities.readerFeatures());
  }
}
