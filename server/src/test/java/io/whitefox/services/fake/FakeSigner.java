package io.whitefox.services.fake;

import io.whitefox.core.TableFile;
import io.whitefox.core.TableFileToBeSigned;
import io.whitefox.core.services.FileSigner;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FakeSigner implements FileSigner {
  @Override
  public TableFile sign(TableFileToBeSigned s) {
    return new TableFile(s.url(), s.url(), s.size(), s.partitionValues(), Long.MAX_VALUE);
  }
}
