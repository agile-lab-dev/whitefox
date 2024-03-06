package io.whitefox.core.services.capabilities;

import java.util.Collections;
import java.util.Set;

public interface ClientCapabilities {
  Set<ReaderFeatures> readerFeatures();

  Set<ResponseFormat> responseFormats();

  /**
   * This is seen from the client perspective, i.e. a parquet client is not compatible with a delta response
   * while the other way around is compatible
   */
  boolean isCompatibleWith(ResponseFormat other);

  ParquetClientCapabilities PARQUET_INSTANCE = new ParquetClientCapabilities();

  static DeltaClientCapabilities delta(Set<ReaderFeatures> readerFeatures) {
    return new DeltaClientCapabilities(readerFeatures);
  }

  static ParquetClientCapabilities parquet() {
    return PARQUET_INSTANCE;
  }

  class DeltaClientCapabilities implements ClientCapabilities {
    private final Set<ReaderFeatures> readerFeatures;

    @Override
    public Set<ReaderFeatures> readerFeatures() {
      return readerFeatures;
    }

    @Override
    public Set<ResponseFormat> responseFormats() {
      return Set.of(ResponseFormat.parquet, ResponseFormat.delta);
    }

    private DeltaClientCapabilities(Set<ReaderFeatures> readerFeatures) {
      this.readerFeatures = Collections.unmodifiableSet(readerFeatures);
    }

    @Override
    public boolean isCompatibleWith(ResponseFormat other) {
      return responseFormats().contains(other);
    }
  }

  class ParquetClientCapabilities implements ClientCapabilities {

    private ParquetClientCapabilities() {}

    @Override
    public Set<ReaderFeatures> readerFeatures() {
      return Set.of();
    }

    @Override
    public Set<ResponseFormat> responseFormats() {
      return Set.of(ResponseFormat.parquet);
    }

    @Override
    public boolean isCompatibleWith(ResponseFormat other) {
      return responseFormats().contains(other);
    }
  }
}
