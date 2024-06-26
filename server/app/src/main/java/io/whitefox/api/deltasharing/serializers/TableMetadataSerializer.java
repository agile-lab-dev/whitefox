package io.whitefox.api.deltasharing.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.whitefox.api.deltasharing.model.v1.TableMetadataResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TableMetadataSerializer implements Serializer<TableMetadataResponse> {
  private final ObjectWriter objectWriter;
  private static final String LINE_FEED = "\n";

  @Inject
  public TableMetadataSerializer(ObjectMapper objectMapper) {
    this.objectWriter = objectMapper.writer();
  }

  @Override
  public String serialize(TableMetadataResponse data) {
    StringBuilder stringBuilder = new StringBuilder();
    try {
      stringBuilder.append(objectWriter.writeValueAsString(data.protocol()));
      stringBuilder.append(LINE_FEED);
      stringBuilder.append(objectWriter.writeValueAsString(data.metadata()));
      return stringBuilder.toString();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
