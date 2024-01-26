package io.whitefox.core.results;

import io.whitefox.core.delta.Metadata;
import io.whitefox.core.delta.Protocol;
import io.whitefox.core.delta.signed.DeltaFileAction;
import io.whitefox.core.delta.unsigned.DeltaFileActionToBeSigned;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class DeltaReadTableResultToBeSigned implements ReadTableResult {
    private final Metadata.ParquetMetadata metadata;
    private final Protocol.ParquetProtocol protocol;
    private final List<DeltaFileActionToBeSigned> files;
}
