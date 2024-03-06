package io.whitefox.core;

import io.whitefox.core.services.capabilities.ClientCapabilities;
import java.util.List;
import java.util.Optional;
import lombok.Value;

public interface ReadTableRequest {

  Optional<List<String>> predicateHints();

  Optional<String> jsonPredicateHints();

  Optional<Long> limitHint();

  ClientCapabilities clientCapabilities();

  @Value
  class ReadTableVersion implements ReadTableRequest {
    Optional<List<String>> predicateHints;
    Optional<String> jsonPredicateHints;
    Optional<Long> limitHint;
    Long version;
    ClientCapabilities clientCapabilities;
  }

  @Value
  class ReadTableAsOfTimestamp implements ReadTableRequest {
    Optional<List<String>> predicateHints;
    Optional<Long> limitHint;
    Optional<String> jsonPredicateHints;
    Long timestamp;
    ClientCapabilities clientCapabilities;
  }

  @Value
  class ReadTableCurrentVersion implements ReadTableRequest {
    Optional<List<String>> predicateHints;
    Optional<String> jsonPredicateHints;
    Optional<Long> limitHint;
    ClientCapabilities clientCapabilities;
  }
}
