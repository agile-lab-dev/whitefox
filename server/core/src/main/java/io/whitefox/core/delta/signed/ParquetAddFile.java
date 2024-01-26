package io.whitefox.core.delta.signed;

import io.whitefox.core.delta.Stats;
import io.whitefox.core.delta.unsigned.ParquetAddFileToBeSigned;
import io.whitefox.core.delta.unsigned.ParquetCDFFileToBeSigned;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class ParquetAddFile implements ParquetFileAction {
    /**
     * An https url that a client can use to read the file directly. The same file in different responses may have different urls
     */
    private final String url;
    /**
     * A unique string for the file in a table. The same file is guaranteed to have the same id across multiple requests. A client may cache the file content and use this id as a key to decide whether to use the cached file content.
     */
    private final String id;
    /**
     * A map from partition column to value for this file. When the table doesn’t have partition columns, this will be an empty map.
     */
    private final Map<String, String> partitionValues;
    /**
     * The size of this file in bytes.
     */
    private final long size;
    /**
     * The timestamp of the file in milliseconds from epoch.
     */
    private final long timestamp;
    /**
     * The table version of this file
     */
    private final int version;
    /**
     * Contains statistics (e.g., count, min/max values for columns) about the data in this file. This field may be missing. A file may or may not have stats. A client can decide whether to use stats or drop it.
     */
    private final Optional<Stats> stats;
    /**
     * The unix timestamp corresponding to the expiration of the url, in milliseconds, returned when the server supports the feature.
     */
    private final Optional<Long> expirationTimestamp;

    @Override
    public Optional<Long> getExpTs() {
        return expirationTimestamp;
    }

    public static ParquetAddFile signed(
            ParquetAddFileToBeSigned f,
            URI signedUrl,
            Optional<Long> expirationTimestamp,
            String newId
    ) {
        return new ParquetAddFile(
                signedUrl.toASCIIString(),
                newId,
                f.getPartitionValues(),
                f.getSize(),
                f.getTimestamp(),
                f.getVersion(),
                f.getStats(),
                expirationTimestamp);
    }
}
