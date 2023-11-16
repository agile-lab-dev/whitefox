package io.whitefox.core.delta;

import io.whitefox.annotations.SkipCoverageGenerated;
import io.whitefox.core.services.DeltaSharingCapabilities;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class Protocol {
    Protocol() {
    }

    Protocol delta(int minReaderVersion, int minWriterVersion, Set<DeltaSharingCapabilities.DeltaSharingFeatures> readerFeatures, Set<DeltaSharingCapabilities.DeltaSharingFeatures> writerFeatures) {
        return new DeltaProtocol(minReaderVersion, minWriterVersion, readerFeatures, writerFeatures);
    }

    ParquetProtocol parquet() {
        return ParquetProtocol.INSTANCE;
    }


    public static class DeltaProtocol extends Protocol {
        private final int minReaderVersion;
        private final int minWriterVersion;

        private final Set<DeltaSharingCapabilities.DeltaSharingFeatures> readerFeatures;

        private final Set<DeltaSharingCapabilities.DeltaSharingFeatures> writerFeatures;

        DeltaProtocol(int minReaderVersion,
                      int minWriterVersion,
                      Set<DeltaSharingCapabilities.DeltaSharingFeatures> readerFeatures,
                      Set<DeltaSharingCapabilities.DeltaSharingFeatures> writerFeatures) {
            this.minReaderVersion = minReaderVersion;
            this.minWriterVersion = minWriterVersion;
            this.readerFeatures = readerFeatures;
            this.writerFeatures = writerFeatures;
        }

        public int minReaderVersion() {
            return minReaderVersion;
        }

        public int minWriterVersion() {
            return minWriterVersion;
        }

        public Set<DeltaSharingCapabilities.DeltaSharingFeatures> readerFeatures() {
            return readerFeatures;
        }

        public Set<DeltaSharingCapabilities.DeltaSharingFeatures> writerFeatures() {
            return writerFeatures;
        }

        @Override
        @SkipCoverageGenerated
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeltaProtocol that = (DeltaProtocol) o;
            return minReaderVersion == that.minReaderVersion && minWriterVersion == that.minWriterVersion && Objects.equals(readerFeatures, that.readerFeatures) && Objects.equals(writerFeatures, that.writerFeatures);
        }

        @Override
        @SkipCoverageGenerated
        public int hashCode() {
            return Objects.hash(minReaderVersion, minWriterVersion, readerFeatures, writerFeatures);
        }

        @Override
        @SkipCoverageGenerated
        public String toString() {
            return "DeltaProtocolObject{" +
                    "minReaderVersion=" + minReaderVersion +
                    ", minWriterVersion=" + minWriterVersion +
                    ", readerFeatures=" + readerFeatures +
                    ", writerFeatures=" + writerFeatures +
                    '}';
        }
    }

    public static class ParquetProtocol extends Protocol {
        private ParquetProtocol(){}

        public static final ParquetProtocol INSTANCE = new ParquetProtocol();
    }

}