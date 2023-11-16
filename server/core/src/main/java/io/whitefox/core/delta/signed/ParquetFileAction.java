package io.whitefox.core.delta.signed;

import java.util.Optional;

public interface ParquetFileAction extends FileAction {
    Optional<Long> getExpTs();
}
