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
        .map(t -> getSnapshotForTimestampAsOf(t.getTime()).thenApply(o -> o.orElseThrow(() -> new RuntimeException("Could not find snapshot for provided timestamp."))))
        .orElse(getSnapshot());
  }

  private CompletionStage<Snapshot> getSnapshot() {
    return deltaLog.thenApply(DeltaLog::snapshot);
  }

  private CompletionStage<Optional<Snapshot>> getSnapshotForTimestampAsOf(long l) {
    return deltaLog
        .thenApply(d -> Optional.of(d.getSnapshotForTimestampAsOf(l)))
        .exceptionally(e ->
                Optional.empty());
  }

  private Timestamp getTimestamp(String timestamp) {
    return new Timestamp(OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        .toInstant()
        .toEpochMilli());
  }
}
