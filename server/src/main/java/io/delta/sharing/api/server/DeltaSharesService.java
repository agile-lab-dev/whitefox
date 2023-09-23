package io.delta.sharing.api.server;

import io.delta.sharing.api.server.model.Share;
import io.delta.sharing.encoders.DeltaPageTokenEncoder;
import io.sharing.persistence.StorageManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DeltaSharesService {
  private final StorageManager storageManager;
  private final Integer defaultMaxResults;
  private final DeltaPageTokenEncoder encoder;

  @Inject
  public DeltaSharesService(
      StorageManager storageManager,
      @ConfigProperty(name = "io.delta.sharing.api.server.defaultMaxResults")
          Integer defaultMaxResults,
      DeltaPageTokenEncoder encoder) {
    this.storageManager = storageManager;
    this.defaultMaxResults = defaultMaxResults;
    this.encoder = encoder;
  }

  public Optional<Share> getShare(String share) {
    return storageManager.getShare(share).map(s -> new Share().name(s.getName()));
  }

  public ImmutablePair<List<Share>, Optional<String>> listShares(
      Optional<String> nextPageToken, Optional<Integer> maxResults) throws IOException {
    List<Share> shares = storageManager.getShares();
    return getPage(
        nextPageToken,
        maxResults,
        shares.size(),
        (s) -> {
          List<Share> sharesResult =
              shares.stream().map((share) -> new Share().name(share.getName())).toList();
          if (sharesResult.isEmpty()) return sharesResult;
          else return sharesResult.subList(s.left, getUpperBound(shares.size(), s.right));
        });
  }

  private Integer getUpperBound(Integer end, Integer maxResults) {
    if (end > maxResults) {
      return maxResults;
    } else return end;
  }

  private <T> ImmutablePair<List<T>, Optional<String>> getPage(
      Optional<String> nextPageToken,
      Optional<Integer> maxResults,
      int totalSize,
      Function<ImmutablePair<Integer, Integer>, List<T>> func)
      throws IOException {
    Integer start =
        nextPageToken
            .map(
                (s) -> {
                  try {
                    return Integer.valueOf(encoder.decodePageToken(s));
                  } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                  }
                })
            .orElse(0);
    if (start > totalSize) {
      throw new RuntimeException(
          String.format("Invalid Next Page Token: token %s is larger than totalSize", start));
    }
    int end = start + maxResults.orElse(defaultMaxResults);
    List<T> result = func.apply(new ImmutablePair<>(start, end));

    Optional<String> optionalToken =
        end < totalSize ? Optional.of(Integer.toString(end)) : Optional.empty();

    // TODO: Handle exceptions to send a response to a client
    return new ImmutablePair<>(
        result,
        optionalToken.map(
            (t) -> {
              try {
                return encoder.encodePageToken(t);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }));
  }
}
