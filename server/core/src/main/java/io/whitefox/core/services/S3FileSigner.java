package io.whitefox.core.services;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import io.whitefox.core.TableFile;
import io.whitefox.core.TableFileIdHashFunction;
import io.whitefox.core.TableFileToBeSigned;
import io.whitefox.core.delta.signed.*;
import io.whitefox.core.delta.unsigned.*;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class S3FileSigner implements FileSigner {

  private final AmazonS3 s3Client;
  private final TableFileIdHashFunction tableFileIdHashFunction;

  public S3FileSigner(AmazonS3 s3Client, TableFileIdHashFunction tableFileIdHashFunction) {
    this.s3Client = s3Client;
    this.tableFileIdHashFunction = tableFileIdHashFunction;
  }

  @Override
  public FileAction sign(FileActionToBeSigned s) {
    if (s instanceof DeltaFileToBeSigned) {
      var delta = (DeltaFileToBeSigned) s;
      var pfa = sign(delta.getDeltaSingleAction());
      return DeltaFile.signed(
              delta,
              pfa.getExpTs(),
              pfa
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
    URI absPath = URI.create(url);
    String bucketName = absPath.getHost();
    String keyName = stripPrefix(absPath.getPath(), "/");
    Date expirationDate = new Date(System.currentTimeMillis() + SECONDS.toMillis(3600));
    return Objects.toString(s3Client.generatePresignedUrl(buildPresignedUrlRequest(bucketName, keyName, expirationDate)));
  }
//  @Override
//  public TableFile sign(TableFileToBeSigned s) {

  //    return new TableFile(
//        presignedUrl.toString(),
//        tableFileIdHashFunction.hash(s.url()),
//        s.size(),
//        Optional.of(s.version()),
//        s.timestamp(),
//        s.partitionValues(),
//        expirationDate.getTime(),
//        Optional.ofNullable(s.stats()));
//  }
//
  private String stripPrefix(String string, String prefix) {
    if (string.startsWith(prefix)) {
      return string.substring(prefix.length());
    } else return string;
  }
  private GeneratePresignedUrlRequest buildPresignedUrlRequest(
      String bucketName, String keyName, Date expirationDate) {
    return new GeneratePresignedUrlRequest(bucketName, keyName)
        .withMethod(HttpMethod.GET)
        .withExpiration(expirationDate);
  }
}
