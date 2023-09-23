package io.delta.sharing.api.server.io.delta.sharing.encoders;

import io.delta.sharing.encoders.DeltaPageTokenEncoder;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DeltaPageTokenEncoderTest {

  DeltaPageTokenEncoder encoder = new DeltaPageTokenEncoder();

  @Test
  public void testTokenEncoding() throws IOException, ClassNotFoundException {
    String id = "SomeId";
    String encoded = encoder.encodePageToken(id);
    String decoded = encoder.decodePageToken(encoded);
    Assertions.assertEquals(id, decoded);
  }
}
