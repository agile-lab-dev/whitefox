package io.whitefox.core.delta.unsigned;

import io.whitefox.core.delta.signed.ParquetRemoveFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;


@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class ParquetRemoveFileToBeSigned implements ParquetFileActionToBeSigned {
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
     * The unix timestamp corresponding to the expiration of the url, in milliseconds, returned when the server supports the feature.
     */
    private final Optional<Long> expirationTimestamp;
}
