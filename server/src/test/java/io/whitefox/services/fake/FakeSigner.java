package io.whitefox.services.fake;

import io.whitefox.core.TableFile;
import io.whitefox.core.TableFileToBeSigned;
import io.whitefox.core.services.FileSigner;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class FakeSigner implements FileSigner {
  @Override
  public TableFile sign(TableFileToBeSigned s) {
    return new TableFile(
        s.url(),
        s.url(), // maybe we can hash this
        s.size(),
        Optional.of(s.version()),
        s.timestamp(),
        s.partitionValues(),
        Long.MAX_VALUE,
        Optional.of(s.stats()));
  }
}
