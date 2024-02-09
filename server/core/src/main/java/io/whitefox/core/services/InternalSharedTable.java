package io.whitefox.core.services;

import io.whitefox.core.ReadTableRequest;
import io.whitefox.core.ReadTableResultToBeSigned;
import io.whitefox.core.services.capabilities.ClientCapabilities;
import java.sql.Timestamp;
import java.util.Optional;

public interface InternalSharedTable {

  Optional<MetadataResponse> getMetadata(
      Optional<Timestamp> startingTimestamp, ClientCapabilities clientCapabilities);

  Optional<Long> getTableVersion(Optional<Timestamp> startingTimestamp);

  ReadTableResultToBeSigned queryTable(ReadTableRequest readTableRequest);
}
