package io.whitefox.core.delta.unsigned;

import io.whitefox.core.delta.signed.DeltaFile;
import io.whitefox.core.delta.signed.FileAction;
import io.whitefox.core.delta.signed.ParquetFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class DeltaFileToBeSigned implements FileActionToBeSigned {

    /**
     * A unique string for the file in a table. The same file is guaranteed to have the same id across multiple requests. A client may cache the file content and use this id as a key to decide whether to use the cached file content.
     */
    private final String id;

    /**
     * A unique string for the deletion vector file in a table. The same deletion vector file is guaranteed to have the same id across multiple requests. A client may cache the file content and use this id as a key to decide whether to use the cached file content.
     */
    private final Optional<String> deletionVectorFileId = Optional.empty();

    /**
     * The table version of the file, returned when querying a table data with a version or timestamp parameter.
     */
    private final Optional<Long> version = Optional.empty();

    /**
     * The unix timestamp corresponding to the table version of the file, in milliseconds, returned when querying a table data with a version or timestamp parameter.
     */
    private final Optional<Long> timestamp = Optional.empty();

    /**
     * The unix timestamp corresponding to the expiration of the url, in milliseconds, returned when the server supports the feature.
     */
    private final Optional<Long> expirationTimestamp = Optional.empty();

    /**
     * Need to be parsed by a delta library as a delta single action, the path field is replaced by pr-signed url.
     */
    private final ParquetFileActionToBeSigned deltaSingleAction;
}
