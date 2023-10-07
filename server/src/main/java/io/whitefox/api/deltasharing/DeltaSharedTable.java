package io.whitefox.api.deltasharing;

import io.delta.standalone.DeltaLog;
import io.delta.standalone.Snapshot;
import io.delta.standalone.actions.Metadata;
import io.whitefox.persistence.memory.PTable;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apache.hadoop.conf.Configuration;

public class DeltaSharedTable {

  private final CompletionStage<DeltaLog> deltaLog;
  private final Configuration configuration;
  private final Path dataPath;

  public DeltaSharedTable(PTable pTable) {
    this.configuration = new Configuration();
    this.dataPath = Path.of(pTable.location());
    this.deltaLog = deltaLog();
  }

  private CompletionStage<DeltaLog> deltaLog() {
    return CompletableFuture.supplyAsync(
        () -> DeltaLog.forTable(configuration, dataPath.toString()));
  }

  public CompletionStage<Metadata> getMetadata(Optional<String> startingTimestamp) {
    return getSnapshot(startingTimestamp).thenApply(Snapshot::getMetadata);
  }

  public CompletionStage<Long> getTableVersion(Optional<String> startingTimestamp) {
    return getSnapshot(startingTimestamp).thenApply(Snapshot::getVersion);
  }

  private CompletionStage<Snapshot> getSnapshot(Optional<String> startingTimestamp) {
    return startingTimestamp
        .map(this::getTimestamp)
        .map(t -> t.thenApply(Timestamp::getTime))
        .map(t -> t.thenCompose(this::getSnapshotForTimestampAsOf))
        .map(snapshot -> snapshot.thenApply(s -> s.orElseThrow(
            () -> new RuntimeException("Could not find snapshot for provided timestamp."))))
        .orElse(getSnapshot());
  }

  private CompletionStage<Snapshot> getSnapshot() {
    return deltaLog.thenApply(DeltaLog::snapshot);
  }

  private CompletionStage<Optional<Snapshot>> getSnapshotForTimestampAsOf(long l) {
    try {
      var snapshot = deltaLog.thenApply(d -> Optional.of(d.getSnapshotForTimestampAsOf(l)));
      return snapshot.handle((s, e) -> {
        if (e instanceof IllegalArgumentException) {
          // IllegalArgumentException - if the timestamp is before the earliest possible snapshot or
          // after the latest possible snapshot
          return Optional.empty();
        } else return s;
      });
    } catch (RuntimeException e) {
      // RuntimeException - if the snapshot is unable to be recreated
      return CompletableFuture.failedFuture(e);
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
