package io.whitefox.core.services;

import io.whitefox.core.*;
import io.whitefox.core.delta.Metadata;
import io.whitefox.core.Schema;
import io.whitefox.core.Share;
import io.whitefox.core.SharedTable;
import io.whitefox.core.results.ReadTableResult;

import java.util.List;
import java.util.Optional;

public interface DeltaSharesService {

  /**
   * @return the table version if it exists, otherwise {@link Optional#empty}
   */
  Optional<Long> getTableVersion(
      String share, String schema, String table, String startingTimestamp);

  /**
   * @return a list (up to maxResults size) of {@link Share} and a token to retrieve the next page.
   * The listing will start from the token passed as nextPageToken (if any, otherwise from the first).
   */
  ContentAndToken<List<Share>> listShares(
      Optional<ContentAndToken.Token> nextPageToken, Optional<Integer> maxResults);

  /**
   * @return the table metadata if exists, otherwise {@link Optional#empty}.
   * This method will also evaluate the input deltaSharingCapabilities and match
   * them with the actual table capabilities.
   */
  Optional<Metadata> getTableMetadata(
      String share,
      String schema,
      String table,
      String startingTimestamp,
      DeltaSharingCapabilities deltaSharingCapabilities);

  /**
   * @return a list (up to maxResults size) of {@link Schema} that are part of input share
   * and a token to retrieve the next page. The listing will start from the token passed as nextPageToken
   * (if any, otherwise from the first). If the share does not exist, it will return {@link Optional#empty()}
   */
  Optional<ContentAndToken<List<Schema>>> listSchemas(
      String share, Optional<ContentAndToken.Token> nextPageToken, Optional<Integer> maxResults);

  /**
   * @return a list (up to maxResults size) of {@link SharedTable} that are part of input share and schema
   * and a token to retrieve the next page. The listing will start from the token passed as nextPageToken
   * (if any, otherwise from the first). If the share or the schema does not exist,
   * it will return {@link Optional#empty()}
   */
  Optional<ContentAndToken<List<SharedTable>>> listTables(
      String share,
      String schema,
      Optional<ContentAndToken.Token> nextPageToken,
      Optional<Integer> maxResults);

  /**
   * @return a list (up to maxResults size) of {@link SharedTable} that are part of input share
   * and a token to retrieve the next page. The listing will start from the token passed as nextPageToken
   * (if any, otherwise from the first). If the share does not exist, it will return {@link Optional#empty()}
   */
  Optional<ContentAndToken<List<SharedTable>>> listTablesOfShare(
      String share, Optional<ContentAndToken.Token> token, Optional<Integer> maxResults);

  /**
   * @return metadata needed to read the table from a delta-sharing client.
   * This method will also evaluate the input deltaSharingCapabilities and match them with the actual table capabilities.
   */
  ReadTableResult queryTable(
      String share,
      String schema,
      String table,
      ReadTableRequest queryRequest,
      DeltaSharingCapabilities capabilities);
}
