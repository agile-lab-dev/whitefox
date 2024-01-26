package io.whitefox.core.services;

import io.whitefox.core.delta.signed.FileAction;
import io.whitefox.core.delta.unsigned.FileActionToBeSigned;

public interface FileSigner {
  FileAction sign(FileActionToBeSigned s);
}
