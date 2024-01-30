package io.whitefox.api.deltasharing.model.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FormatSerializationTest {
  ObjectMapper om = new ObjectMapper();

  @Test
  void serializationTest() throws JsonProcessingException {
    var expected = "{\"provider\":\"parquet\"}";
    Assertions.assertEquals(expected, om.writer().writeValueAsString(new Format()));
  }

  @Test
  void deserializationTest() throws IOException {
    var input = "{\"provider\":\"parquet\"}";
    var result = om.reader().readValue(input, Format.class);
    Assertions.assertEquals(new Format(), result);
  }
}
