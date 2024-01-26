package io.whitefox.core.results;

import io.whitefox.core.delta.Metadata;
import io.whitefox.core.delta.Protocol;
import io.whitefox.core.delta.signed.ParquetFileAction;
import io.whitefox.core.delta.unsigned.ParquetFileActionToBeSigned;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class ParquetReadTableResultToBeSigned implements ReadTableResult {
    private final Metadata.ParquetMetadata metadata;
    private final Protocol.ParquetProtocol protocol;
    private final List<ParquetFileActionToBeSigned> files;
}
