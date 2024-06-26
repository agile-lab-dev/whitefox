package io.whitefox.core.services;

import io.whitefox.core.*;
import io.whitefox.core.services.capabilities.ClientCapabilities;
import io.whitefox.core.services.capabilities.ResponseFormat;
import io.whitefox.core.services.exceptions.IncompatibleTableWithClient;
import io.whitefox.core.services.exceptions.TableNotFound;
import io.whitefox.persistence.StorageManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DeltaSharesServiceImpl implements DeltaSharesService {

  private final StorageManager storageManager;
  private final Integer defaultMaxResults;
  private final TableLoaderFactory tableLoaderFactory;
  private final FileSignerFactory fileSignerFactory;

  @Inject
  public DeltaSharesServiceImpl(
      StorageManager storageManager,
      @ConfigProperty(name = "io.delta.sharing.api.server.defaultMaxResults")
          Integer defaultMaxResults,
      TableLoaderFactory tableLoaderFactory,
      FileSignerFactory signerFactory) {
    this.storageManager = storageManager;
    this.defaultMaxResults = defaultMaxResults;
    this.tableLoaderFactory = tableLoaderFactory;
    this.fileSignerFactory = signerFactory;
  }

  @Override
  public Optional<Long> getTableVersion(
      String share, String schema, String table, Optional<Timestamp> startingTimestamp) {
    return storageManager
        .getSharedTable(share, schema, table)
        .map(t -> tableLoaderFactory
            .newTableLoader(t.internalTable())
            .loadTable(t)
            .getTableVersion(startingTimestamp))
        .orElse(Optional.empty());
  }

  @Override
  public ContentAndToken<List<Share>> listShares(
      Optional<ContentAndToken.Token> nextPageToken, Optional<Integer> maxResults) {
    Integer finalMaxResults = maxResults.orElse(defaultMaxResults);
    Integer start = nextPageToken.map(ContentAndToken.Token::value).orElse(0);
    var pageContent = storageManager.getShares(start, finalMaxResults);
    int end = start + finalMaxResults;
    Optional<ContentAndToken.Token> optionalToken =
        end < pageContent.size() ? Optional.of(new ContentAndToken.Token(end)) : Optional.empty();
    var content = pageContent.result();
    return optionalToken
        .map(t -> ContentAndToken.of(content, t))
        .orElse(ContentAndToken.withoutToken(content));
  }

  @Override
  public Optional<Metadata> getTableMetadata(
      String share,
      String schema,
      String tableName,
      Optional<Timestamp> startingTimestamp,
      ClientCapabilities clientCapabilities) {
    var table = storageManager
        .getSharedTable(share, schema, tableName)
        .map(t -> tableLoaderFactory.newTableLoader(t.internalTable()).loadTable(t));
    return table
        .flatMap(t -> t.getMetadata(startingTimestamp))
        .map(m -> checkResponseFormat(clientCapabilities, Metadata::format, m, tableName));
  }

  @Override
  public Optional<ContentAndToken<List<Schema>>> listSchemas(
      String share, Optional<ContentAndToken.Token> nextPageToken, Optional<Integer> maxResults) {
    Integer finalMaxResults = maxResults.orElse(defaultMaxResults);
    Integer start = nextPageToken.map(ContentAndToken.Token::value).orElse(0);
    var optPageContent = storageManager.listSchemas(share, start, finalMaxResults);
    int end = start + finalMaxResults;

    return optPageContent.map(pageContent -> {
      Optional<ContentAndToken.Token> optionalToken =
          end < pageContent.size() ? Optional.of(new ContentAndToken.Token(end)) : Optional.empty();
      var content = pageContent.result();
      return optionalToken
          .map(t -> ContentAndToken.of(content, t))
          .orElse(ContentAndToken.withoutToken(content));
    });
  }

  @Override
  public Optional<ContentAndToken<List<SharedTable>>> listTables(
      String share,
      String schema,
      Optional<ContentAndToken.Token> nextPageToken,
      Optional<Integer> maxResults) {
    Integer finalMaxResults = maxResults.orElse(defaultMaxResults);
    Integer start = nextPageToken.map(ContentAndToken.Token::value).orElse(0);
    var optPageContent = storageManager.listTables(share, schema, start, finalMaxResults);
    int end = start + finalMaxResults;
    return optPageContent.map(pageContent -> {
      Optional<ContentAndToken.Token> optionalToken =
          end < pageContent.size() ? Optional.of(new ContentAndToken.Token(end)) : Optional.empty();
      var content = pageContent.result();
      return optionalToken
          .map(t -> ContentAndToken.of(content, t))
          .orElse(ContentAndToken.withoutToken(content));
    });
  }

  @Override
  public Optional<ContentAndToken<List<SharedTable>>> listTablesOfShare(
      String share, Optional<ContentAndToken.Token> nextPageToken, Optional<Integer> maxResults) {
    Integer finalMaxResults = maxResults.orElse(defaultMaxResults);
    Integer start = nextPageToken.map(ContentAndToken.Token::value).orElse(0);
    var optPageContent = storageManager.listTablesOfShare(share, start, finalMaxResults);
    int end = start + finalMaxResults;
    return optPageContent.map(pageContent -> {
      Optional<ContentAndToken.Token> optionalToken =
          end < pageContent.size() ? Optional.of(new ContentAndToken.Token(end)) : Optional.empty();
      return optionalToken
          .map(t -> ContentAndToken.of(pageContent.result(), t))
          .orElse(ContentAndToken.withoutToken(pageContent.result()));
    });
  }

  @Override
  @SneakyThrows
  public ReadTableResult queryTable(
      String share,
      String schema,
      String tableName,
      ReadTableRequest queryRequest,
      ClientCapabilities clientCapabilities) {
    SharedTable sharedTable = storageManager
        .getSharedTable(share, schema, tableName)
        .orElseThrow(() -> new TableNotFound(String.format(
            "Table %s not found in share %s with schema %s", tableName, share, schema)));

    try (FileSigner fileSigner =
        fileSignerFactory.newFileSigner(sharedTable.internalTable().provider().storage())) {
      var readTableResultToBeSigned = tableLoaderFactory
          .newTableLoader(sharedTable.internalTable())
          .loadTable(sharedTable)
          .queryTable(queryRequest);
      return checkResponseFormat(
          clientCapabilities,
          ReadTableResult::responseFormat,
          new ReadTableResult(
              readTableResultToBeSigned.protocol(),
              readTableResultToBeSigned.metadata(),
              readTableResultToBeSigned.other().stream()
                  .map(fileSigner::sign)
                  .collect(Collectors.toList()),
              readTableResultToBeSigned.version(),
              ResponseFormat.parquet),
          tableName);
    }
  }

  private <A> A checkResponseFormat(
      ClientCapabilities clientCapabilities,
      Function<A, ResponseFormat> formatExtractor,
      A formatContainer,
      String tableName) {
    if (!clientCapabilities
        .responseFormat()
        .isCompatibleWith(formatExtractor.apply(formatContainer))) {
      throw new IncompatibleTableWithClient(
          "Table " + tableName + " is not compatible with client " + clientCapabilities);
    } else {
      return formatContainer;
    }
  }
}
