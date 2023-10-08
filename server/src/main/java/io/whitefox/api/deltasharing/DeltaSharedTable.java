package io.whitefox.api.deltasharing;

import io.delta.standalone.DeltaLog;
import io.delta.standalone.Snapshot;
import io.delta.standalone.actions.Metadata;
import io.whitefox.persistence.memory.PTable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apache.hadoop.conf.Configuration;

public class DeltaSharedTable {

  private final DeltaLog deltaLog;
  private final Configuration configuration;
  private final Path dataPath;

  private DeltaSharedTable(DeltaLog deltaLog, Configuration configuration, Path dataPath) {

    this.configuration = configuration;
    this.dataPath = dataPath;
    this.deltaLog = deltaLog;
  }

  public static CompletionStage<DeltaSharedTable> of(PTable table) {
    var configuration = new Configuration();
    var dataPath = Paths.get(table.location());
    return CompletableFuture.supplyAsync(
            () -> DeltaLog.forTable(configuration, dataPath.toString()))
        .thenApplyAsync(dl -> new DeltaSharedTable(dl, configuration, dataPath));
  }


  public CompletionStage<Optional<Metadata>> getMetadata(Optional<String> startingTimestamp) {
    return getSnapshot(startingTimestamp).thenApply(o -> o.map(Snapshot::getMetadata));
  }

  public CompletionStage<Optional<Long>> getTableVersion(Optional<String> startingTimestamp) {
    return getSnapshot(startingTimestamp).thenApply(o -> o.map(Snapshot::getVersion));
  }

  private CompletionStage<Optional<Snapshot>> getSnapshot(Optional<String> startingTimestamp) {
    return startingTimestamp
        .map(this::getTimestamp)
        .map(t -> t.thenApply(Timestamp::getTime))
        .map(t -> t.thenCompose(this::getSnapshotForTimestampAsOf))
        .orElse(getSnapshot().thenApply(Optional::of));
  }

  private CompletionStage<Snapshot> getSnapshot() {
    return CompletableFuture.completedFuture(deltaLog.snapshot());
  }

  private CompletionStage<Optional<Snapshot>> getSnapshotForTimestampAsOf(long l) {
    try {
      return CompletableFuture.completedStage(Optional.of(deltaLog.getSnapshotForTimestampAsOf(l)));
    } catch (IllegalArgumentException iea) {
      return CompletableFuture.completedFuture(Optional.empty());
    } catch (Throwable t) {
      return CompletableFuture.failedFuture(t);
    }
  }

  private CompletionStage<Timestamp> getTimestamp(String timestamp) {
    try {
      return CompletableFuture.completedStage(
          new Timestamp(OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
              .toInstant()
              .toEpochMilli()));
    } catch (Throwable e) {
      return CompletableFuture.failedFuture(e);
    }
  }
}
