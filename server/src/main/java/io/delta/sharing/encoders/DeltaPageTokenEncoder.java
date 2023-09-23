package io.delta.sharing.encoders;

import static java.nio.charset.StandardCharsets.UTF_8;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.*;
import java.util.Base64;

@ApplicationScoped
public class DeltaPageTokenEncoder {

  public String encodePageToken(String id) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
        oos.writeObject(new PageToken(id));
        return new String(Base64.getUrlEncoder().encode(baos.toByteArray()), UTF_8);
      }
    } catch (IOException e) {
      e.printStackTrace();
      // TODO: Define custom exceptions for handling ClassNotFoundException/IOException
      throw e;
    }
  }

  public String decodePageToken(String encodedPageToken)
      throws IOException, ClassNotFoundException {
    byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedPageToken.getBytes(UTF_8));
    try (ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes)) {
      try (ObjectInputStream ois = new ObjectInputStream(bais)) {
        PageToken pg = (PageToken) ois.readObject();
        return pg.getId();
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      // TODO: Define custom exceptions for handling ClassNotFoundException/IOException
      throw e;
    }
  }
}
