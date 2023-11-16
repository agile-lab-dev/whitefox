package io.whitefox.core.results;

import io.whitefox.core.delta.Metadata;
import io.whitefox.core.delta.signed.ParquetFileAction;
import io.whitefox.core.delta.Protocol;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class ParquetReadTableResult implements ReadTableResult {
    private final Metadata.ParquetMetadata metadata;
    private final Protocol.ParquetProtocol protocol;
    private final List<ParquetFileAction> files;
}
