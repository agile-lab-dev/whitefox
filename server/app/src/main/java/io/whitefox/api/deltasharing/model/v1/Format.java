package io.whitefox.api.deltasharing.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class Format {
  @JsonProperty
  String provider = "parquet";
}
