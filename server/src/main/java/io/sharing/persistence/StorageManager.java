package io.sharing.persistence;

import io.delta.sharing.api.server.model.Share;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public interface StorageManager {
  CompletionStage<Optional<Share>> getShare(String share);

  // no need for CompletionStage because stream is already "lazy"
  ResultAndTotalSize<Stream<Share>> getShares();
}
