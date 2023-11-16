package io.whitefox.core.services;

import io.whitefox.core.TableFile;
import io.whitefox.core.TableFileToBeSigned;
import io.whitefox.core.delta.signed.*;
import io.whitefox.core.delta.unsigned.*;

import java.util.Optional;

public class NoOpSigner implements FileSigner {
  @Override
  public FileAction sign(FileActionToBeSigned s) {
    if (s instanceof DeltaFileToBeSigned){
      var delta = (DeltaFileToBeSigned) s;
      var pfa = sign(delta.getDeltaSingleAction());
      return DeltaFile.signed(
              delta,
              pfa,
              pfa.getExpTs(),
              delta.getId()
      );
    } else if (s instanceof ParquetFileActionToBeSigned) {
      return sign((ParquetFileActionToBeSigned) s);
    } else {
      throw new IllegalArgumentException(String.format("Unknown FileAction to be signed %s", s));
    }
  }

  private ParquetFileAction sign(ParquetFileActionToBeSigned tbs) {
    if (tbs instanceof ParquetFileToBeSigned) {
      var f = (ParquetFileToBeSigned) tbs;
      return ParquetFile.signed(
              f,
              f.getId(),
              f.getExpirationTimestamp(),
              sign(f.getUrl())
      );
    } else if (tbs instanceof ParquetAddFileToBeSigned) {
      var f = (ParquetAddFileToBeSigned) tbs;
      return ParquetAddFile.signed(
              f,
              f.getExpirationTimestamp(),
              sign(f.getUrl())
      );
    } else if (tbs instanceof ParquetCDFFileToBeSigned) {
      var f = (ParquetCDFFileToBeSigned) tbs;
      return ParquetCDFFile.signed(
              f,
              f.getExpirationTimestamp(),
              sign(f.getUrl())
      );
    } else if (tbs instanceof ParquetRemoveFileToBeSigned) {
      var f = (ParquetRemoveFileToBeSigned) tbs;
      return ParquetRemoveFile.signed(
              f,
              f.getExpirationTimestamp(),
              sign(f.getUrl())
      );
    } else {
      throw new IllegalArgumentException(String.format("Unknown FileAction to be signed %s", tbs));
    }
  }

  private String sign(String url) {
    return url;
  }
}
