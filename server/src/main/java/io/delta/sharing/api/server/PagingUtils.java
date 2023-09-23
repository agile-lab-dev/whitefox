package io.delta.sharing.api.server;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PagingUtils {
  public static <T> CompletionStage<List<T>> getPage(
      int offset, int maxResults, int totalSize, Stream<T> data, ExecutorService executorService) {
    if (offset > totalSize) {
      throw new RuntimeException(
          String.format("Invalid Next Page Token: token %s is larger than totalSize", offset));
    }
    return CompletableFuture.supplyAsync(
        () -> data.skip(offset).limit(maxResults).collect(Collectors.toList()), executorService);
  }
}
