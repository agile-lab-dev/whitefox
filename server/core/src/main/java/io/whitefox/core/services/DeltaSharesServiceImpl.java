package io.whitefox.core.services;

import io.whitefox.core.*;
import io.whitefox.core.services.capabilities.ClientCapabilities;
import io.whitefox.core.services.capabilities.ResponseFormat;
import io.whitefox.core.services.exceptions.IncompatibleTableWithClient;
import io.whitefox.core.services.exceptions.ShareNotFound;
import io.whitefox.core.services.exceptions.TableNotFound;
import io.whitefox.persistence.StorageManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
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
  private final WhitefoxAuthorization whitefoxAuthorization;

  @Inject
  public DeltaSharesServiceImpl(
      StorageManager storageManager,
      @ConfigProperty(name = "io.delta.sharing.api.server.defaultMaxResults")
          Integer defaultMaxResults,
      TableLoaderFactory tableLoaderFactory,
      FileSignerFactory signerFactory,
      WhitefoxAuthorization whitefoxAuthorization) {
    this.storageManager = storageManager;
    this.defaultMaxResults = defaultMaxResults;
    this.tableLoaderFactory = tableLoaderFactory;
    this.fileSignerFactory = signerFactory;
    this.whitefoxAuthorization = whitefoxAuthorization;
  }

  @Override
  public Optional<Long> getTableVersion(
      String share,
      String schema,
      String table,
      Optional<Timestamp> startingTimestamp,
      Principal currentPrincipal) {
    return storageManager
        .getShare(share)
        .map(s -> {
          var authorization = whitefoxAuthorization.authorize(s, currentPrincipal);
          if (!authorization) throw new ForbiddenException();
          return storageManager.getSharedTable(share, schema, table).flatMap(t -> tableLoaderFactory
              .newTableLoader(t.internalTable())
              .loadTable(t)
              .getTableVersion(startingTimestamp));
        })
        .orElse(Optional.empty());
  }

  @Override
  public ContentAndToken<List<Share>> listShares(
      Optional<ContentAndToken.Token> nextPageToken,
      Optional<Integer> maxResults,
      Principal currentPrincipal) {
    Integer finalMaxResults = maxResults.orElse(defaultMaxResults);
    Integer start = nextPageToken.map(ContentAndToken.Token::value).orElse(0);
    var pageContent = storageManager.getShares(start, finalMaxResults);
    int end = start + finalMaxResults;
    Optional<ContentAndToken.Token> optionalToken =
        end < pageContent.size() ? Optional.of(new ContentAndToken.Token(end)) : Optional.empty();
    var content = pageContent.result().stream()
        .filter(
            s -> s.recipients().contains(currentPrincipal) || s.owner().equals(currentPrincipal))
        .collect(Collectors.toList());
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
      ClientCapabilities clientCapabilities,
      Principal currentPrincipal) {
    return storageManager
        .getShare(share)
        .map(s -> {
          var authorization = whitefoxAuthorization.authorize(s, currentPrincipal);
          if (!authorization) throw new ForbiddenException();
          var table = storageManager
              .getSharedTable(share, schema, tableName)
              .map(t -> tableLoaderFactory.newTableLoader(t.internalTable()).loadTable(t));
          return table
              .flatMap(t -> t.getMetadata(startingTimestamp))
              .map(m -> checkResponseFormat(clientCapabilities, Metadata::format, m, tableName));
        })
        .orElse(Optional.empty());
  }

  @Override
  public Optional<ContentAndToken<List<Schema>>> listSchemas(
      String share,
      Optional<ContentAndToken.Token> nextPageToken,
      Optional<Integer> maxResults,
      Principal currentPrincipal) {
    return storageManager
        .getShare(share)
        .map(s -> {
          var authorization = whitefoxAuthorization.authorize(s, currentPrincipal);
          if (!authorization) throw new ForbiddenException();
          Integer finalMaxResults = maxResults.orElse(defaultMaxResults);
          Integer start = nextPageToken.map(ContentAndToken.Token::value).orElse(0);
          var optPageContent = storageManager.listSchemas(share, start, finalMaxResults);
          int end = start + finalMaxResults;

          return optPageContent.map(pageContent -> {
            Optional<ContentAndToken.Token> optionalToken = end < pageContent.size()
                ? Optional.of(new ContentAndToken.Token(end))
                : Optional.empty();
            var content = pageContent.result();
            return optionalToken
                .map(t -> ContentAndToken.of(content, t))
                .orElse(ContentAndToken.withoutToken(content));
          });
        })
        .orElse(Optional.empty());
  }

  @Override
  public Optional<ContentAndToken<List<SharedTable>>> listTables(
      String share,
      String schema,
      Optional<ContentAndToken.Token> nextPageToken,
      Optional<Integer> maxResults,
      Principal currentPrincipal) {
    return storageManager
        .getShare(share)
        .map(s -> {
          var authorization = whitefoxAuthorization.authorize(s, currentPrincipal);
          if (!authorization) throw new ForbiddenException();
          Integer finalMaxResults = maxResults.orElse(defaultMaxResults);
          Integer start = nextPageToken.map(ContentAndToken.Token::value).orElse(0);
          var optPageContent = storageManager.listTables(share, schema, start, finalMaxResults);
          int end = start + finalMaxResults;
          return optPageContent.map(pageContent -> {
            Optional<ContentAndToken.Token> optionalToken = end < pageContent.size()
                ? Optional.of(new ContentAndToken.Token(end))
                : Optional.empty();
            var content = pageContent.result();
            return optionalToken
                .map(t -> ContentAndToken.of(content, t))
                .orElse(ContentAndToken.withoutToken(content));
          });
        })
        .orElse(Optional.empty());
  }

  @Override
  public Optional<ContentAndToken<List<SharedTable>>> listTablesOfShare(
      String share,
      Optional<ContentAndToken.Token> nextPageToken,
      Optional<Integer> maxResults,
      Principal currentPrincipal) {
    return storageManager
        .getShare(share)
        .map(s -> {
          var authorization = whitefoxAuthorization.authorize(s, currentPrincipal);
          if (!authorization)
            throw new ForbiddenException(
                "Principal: " + currentPrincipal + " cannot access share: " + share);
          Integer finalMaxResults = maxResults.orElse(defaultMaxResults);
          Integer start = nextPageToken.map(ContentAndToken.Token::value).orElse(0);
          var optPageContent = storageManager.listTablesOfShare(share, start, finalMaxResults);
          int end = start + finalMaxResults;
          return optPageContent.map(pageContent -> {
            Optional<ContentAndToken.Token> optionalToken = end < pageContent.size()
                ? Optional.of(new ContentAndToken.Token(end))
                : Optional.empty();
            return optionalToken
                .map(t -> ContentAndToken.of(pageContent.result(), t))
                .orElse(ContentAndToken.withoutToken(pageContent.result()));
          });
        })
        .orElse(Optional.empty());
  }

  @Override
  @SneakyThrows
  public ReadTableResult queryTable(
      String share,
      String schema,
      String tableName,
      ReadTableRequest queryRequest,
      ClientCapabilities clientCapabilities,
      Principal currentPrincipal) {
    return storageManager
        .getShare(share)
        .map(s -> {
          var authorization = whitefoxAuthorization.authorize(s, currentPrincipal);
          if (!authorization)
            throw new ForbiddenException(
                "Principal: " + currentPrincipal + " cannot access share: " + share);

          SharedTable sharedTable = storageManager
              .getSharedTable(share, schema, tableName)
              .orElseThrow(() -> new TableNotFound(String.format(
                  "Table %s not found in share %s with schema %s", tableName, share, schema)));

          var readTableResultToBeSigned = tableLoaderFactory
              .newTableLoader(sharedTable.internalTable())
              .loadTable(sharedTable)
              .queryTable(queryRequest);
          try (FileSigner fileSigner = fileSignerFactory.newFileSigner(
              sharedTable.internalTable().provider().storage())) {
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
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .orElseThrow(() -> new ShareNotFound(String.format("Share %s not found", share)));
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
