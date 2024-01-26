package io.whitefox.core.delta.unsigned;

import io.whitefox.core.delta.Stats;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
public class ParquetFileToBeSigned implements ParquetFileActionToBeSigned {

    /**
     * A https url that a client can use to read the file directly. The same file in different responses may have different urls.
     */
    private final String url;

    /**
     * A unique string for the file in a table. The same file is guaranteed to have the same id across multiple requests. A client may cache the file content and use this id as a key to decide whether to use the cached file content.
     */
    private final String id;

    /**
     * A map from partition column to value for this file.
     */
    private final Map<String, String> partitionValues;

    /**
     * The size of this file in bytes
     */
    private final long size;

    /**
     * Contains statistics (e.g., count, min/max values for columns) about the data in this file. This field may be missing.
     */
    private final Optional<Stats> stats;

    /**
     * The table version of the file, returned when querying a table data with a version or timestamp parameter.
     */
    private final Optional<Long> version;

    /**
     * The unix timestamp corresponding to the table version of the file, in milliseconds, returned when querying a table data with a version or timestamp parameter.
     */
    private final Optional<Long> timestamp;

    /**
     * The unix timestamp corresponding to the expiration of the url, in milliseconds, returned when the server supports the feature.
     */
    private final Optional<Long> expirationTimestamp;
}
