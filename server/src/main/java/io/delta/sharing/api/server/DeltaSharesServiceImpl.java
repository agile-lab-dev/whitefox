package io.delta.sharing.api.server;

import io.delta.sharing.api.ContentAndToken;
import io.delta.sharing.api.server.model.Share;
import io.delta.sharing.encoders.DeltaPageTokenEncoder;
import io.sharing.persistence.StorageManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DeltaSharesServiceImpl implements DeltaSharesService {

  private final StorageManager storageManager;
  private final Integer defaultMaxResults;
  private final DeltaPageTokenEncoder encoder;
  private final IOExecutorService ioExecutorService;

  @Inject
  public DeltaSharesServiceImpl(
      StorageManager storageManager,
      @ConfigProperty(name = "io.delta.sharing.api.server.defaultMaxResults")
          Integer defaultMaxResults,
      DeltaPageTokenEncoder encoder,
      IOExecutorService ioExecutorService) {
    this.storageManager = storageManager;
    this.defaultMaxResults = defaultMaxResults;
    this.encoder = encoder;
    this.ioExecutorService = ioExecutorService;
  }

  @Override
  public CompletionStage<Optional<Share>> getShare(String share) {
    return storageManager.getShare(share);
  }

  @Override
  public CompletionStage<ContentAndToken<List<Share>>> listShares(
      Optional<ContentAndToken.Token> nextPageToken, Optional<Integer> maxResults) {
    Integer finalMaxResults = maxResults.orElse(defaultMaxResults);
    var resAndSize = storageManager.getShares();
    return getPage(nextPageToken, finalMaxResults, resAndSize.size, resAndSize.result);
  }

  private <T> CompletionStage<ContentAndToken<List<T>>> getPage(
      Optional<ContentAndToken.Token> nextPageToken,
      int maxResults,
      int totalSize,
      Stream<T> func) {
    Integer start =
        nextPageToken.map(s -> Integer.valueOf(encoder.decodePageToken(s.value))).orElse(0);
    if (start > totalSize) {
      throw new RuntimeException(
          String.format("Invalid Next Page Token: token %s is larger than totalSize", start));
    }
    int end = start + maxResults;
    Optional<String> optionalToken =
        end < totalSize ? Optional.of(Integer.toString(end)) : Optional.empty();

    return CompletableFuture.supplyAsync(
            () -> func.skip(start).limit(maxResults).collect(Collectors.toList()),
            ioExecutorService)
        .whenComplete((x, y) -> func.close())
        .thenApply(
            pageContent ->
                optionalToken
                    .map(encoder::encodePageToken)
                    .map(t -> ContentAndToken.of(pageContent, t))
                    .orElse(ContentAndToken.withoutToken(pageContent)));
  }
}
