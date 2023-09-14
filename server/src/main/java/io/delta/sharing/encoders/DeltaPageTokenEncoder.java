package io.delta.sharing.encoders;

import static java.nio.charset.StandardCharsets.UTF_8;

import jakarta.inject.Singleton;
import java.util.Base64;

@Singleton
public class DeltaPageTokenEncoder {

  public String encodePageToken(String id) {
    return new String(Base64.getUrlEncoder().encode(id.getBytes(UTF_8)), UTF_8);
  }

  public String decodePageToken(String encodedPageToken) {
    return new String(Base64.getMimeDecoder().decode(encodedPageToken), UTF_8);
  }
}
