package io.delta.sharing.api.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.delta.sharing.api.server.model.Share;
import io.delta.sharing.encoders.DeltaPageTokenEncoder;
import io.quarkus.test.junit.QuarkusTest;
import io.sharing.persistence.StorageManager;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

@QuarkusTest
public class DeltaShareServiceTest {

  DeltaPageTokenEncoder encoder = new DeltaPageTokenEncoder();
  Integer defaultMaxResults = 10;

  @Test
  public void getUnknownShare() {
    DeltaSharesService deltaSharesService =
        new DeltaSharesService(new StorageManager(), defaultMaxResults, encoder);
    Optional<Share> unknown = deltaSharesService.getShare("unknown");
    assertEquals(Optional.empty(), unknown);
  }

  @Test
  public void getShare() {
    ConcurrentMap<String, Share> shares = new ConcurrentHashMap<>();
    shares.put("key", new Share().id("key").name("name"));
    DeltaSharesService deltaSharesService =
        new DeltaSharesService(new StorageManager(shares), defaultMaxResults, encoder);
    Optional<Share> share = deltaSharesService.getShare("key");
    assertTrue(share.isPresent());
    assertEquals("name", share.get().getName());
    assertTrue(StringUtils.isBlank(share.get().getId()));
  }

  @Test
  public void listShares() throws IOException {
    ConcurrentMap<String, Share> shares = new ConcurrentHashMap<>();
    shares.put("key", new Share().id("key").name("name"));
    DeltaSharesService deltaSharesService =
        new DeltaSharesService(new StorageManager(shares), defaultMaxResults, encoder);
    ImmutablePair<List<Share>, Optional<String>> sharesWithNextToken =
        deltaSharesService.listShares(Optional.empty(), Optional.of(30));
    assertEquals(1, sharesWithNextToken.left.size());
    assertTrue(sharesWithNextToken.right.isEmpty());
  }

  @Test
  public void listSharesWithToken() throws IOException {
    ConcurrentMap<String, Share> shares = new ConcurrentHashMap<>();
    shares.put("key", new Share().id("key").name("name"));
    DeltaSharesService deltaSharesService =
        new DeltaSharesService(new StorageManager(shares), defaultMaxResults, encoder);
    ImmutablePair<List<Share>, Optional<String>> sharesWithNextToken =
        deltaSharesService.listShares(Optional.of(encoder.encodePageToken("0")), Optional.of(30));
    assertEquals(1, sharesWithNextToken.left.size());
    assertTrue(sharesWithNextToken.right.isEmpty());
  }
}
