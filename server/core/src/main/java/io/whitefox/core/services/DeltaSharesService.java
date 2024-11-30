package io.whitefox.core.services;

import io.whitefox.core.*;
import io.whitefox.core.Metadata;
import io.whitefox.core.Schema;
import io.whitefox.core.Share;
import io.whitefox.core.SharedTable;
import io.whitefox.core.services.capabilities.ClientCapabilities;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface DeltaSharesService {

  Optional<Long> getTableVersion(
      String share,
      String schema,
      String table,
      Optional<Timestamp> startingTimestamp,
      Principal principal);

  ContentAndToken<List<Share>> listShares(
      Optional<ContentAndToken.Token> nextPageToken,
      Optional<Integer> maxResults,
      Principal currentPrincipal);

  Optional<Metadata> getTableMetadata(
      String share,
      String schema,
      String table,
      Optional<Timestamp> startingTimestamp,
      ClientCapabilities clientCapabilities,
      Principal currentPrincipal);

  Optional<ContentAndToken<List<Schema>>> listSchemas(
      String share,
      Optional<ContentAndToken.Token> nextPageToken,
      Optional<Integer> maxResults,
      Principal currentPrincipal);

  Optional<ContentAndToken<List<SharedTable>>> listTables(
      String share,
      String schema,
      Optional<ContentAndToken.Token> nextPageToken,
      Optional<Integer> maxResults,
      Principal currentPrincipal);

  Optional<ContentAndToken<List<SharedTable>>> listTablesOfShare(
      String share,
      Optional<ContentAndToken.Token> token,
      Optional<Integer> maxResults,
      Principal currentPrincipal);

  ReadTableResult queryTable(
      String share,
      String schema,
      String table,
      ReadTableRequest queryRequest,
      ClientCapabilities clientCapabilities,
      Principal currentPrincipal);
}
