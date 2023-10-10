package io.whitefox.persistence.memory;

import io.whitefox.api.deltasharing.encoders.InvalidPageTokenException;
import io.whitefox.core.*;
import io.whitefox.persistence.DuplicateKeyException;
import io.whitefox.persistence.StorageManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class InMemoryStorageManager implements StorageManager {
  private final ConcurrentMap<String, Share> shares;
  private final ConcurrentMap<String, Metastore> metastores;

  @Inject
  public InMemoryStorageManager() {
    this.shares = new ConcurrentHashMap<>();
    metastores = new ConcurrentHashMap<>();
  }

  public InMemoryStorageManager(List<Share> shares, List<Metastore> metastores) {
    this.shares = new ConcurrentHashMap<>(
        shares.stream().collect(Collectors.toMap(Share::name, Function.identity())));
    this.metastores = new ConcurrentHashMap<>(
        metastores.stream().collect(Collectors.toMap(Metastore::name, Function.identity())));
  }

  public InMemoryStorageManager(List<Share> shares) {
    this(shares, Collections.emptyList());
  }

  @Override
  public Optional<Share> getShare(String share) {
    return Optional.ofNullable(shares.get(share));
  }

  @Override
  public Optional<Table> getTable(String share, String schema, String table) {

    return Optional.ofNullable(shares.get(share))
        .flatMap(shareObj -> Optional.ofNullable(shareObj.schemas().get(schema)))
        .flatMap(schemaObj ->
            schemaObj.tables().stream().filter(t -> (t.name().equals(table))).findFirst());
  }

  @Override
  public ResultAndTotalSize<List<Share>> getShares(int offset, int maxResultSize) {
    var totalSize = shares.size();
    if (offset > totalSize) {
      throw new InvalidPageTokenException(
          String.format("Invalid Next Page Token: token %s is larger than totalSize", offset));
    } else {
      return new ResultAndTotalSize<>(
          shares.values().stream().skip(offset).limit(maxResultSize).collect(Collectors.toList()),
          totalSize);
    }
  }

  @Override
  public Optional<ResultAndTotalSize<List<Schema>>> listSchemas(
      String share, int offset, int maxResultSize) {
    return Optional.ofNullable(shares.get(share)).flatMap(shareObj -> {
      var schemaMap = shareObj.schemas();
      var totalSize = schemaMap.size();
      if (offset > totalSize) {
        throw new InvalidPageTokenException(
            String.format("Invalid Next Page Token: token %s is larger than totalSize", offset));
      }
      return Optional.of(new ResultAndTotalSize<>(
          schemaMap.values().stream()
              .skip(offset)
              .limit(maxResultSize)
              .collect(Collectors.toList()),
          totalSize));
    });
  }

  @Override
  public Optional<ResultAndTotalSize<List<Table>>> listTables(
      String share, String schema, int offset, int maxResultSize) {
    return Optional.ofNullable(shares.get(share))
        .flatMap(shareObj -> Optional.ofNullable(shareObj.schemas().get(schema)))
        .flatMap(schemaObj -> {
          var tableList = schemaObj.tables();
          var totalSize = tableList.size();
          if (offset > totalSize) {
            throw new InvalidPageTokenException(String.format(
                "Invalid Next Page Token: token %s is larger than totalSize", offset));
          } else {
            return Optional.of(new ResultAndTotalSize<>(
                tableList.stream().skip(offset).limit(maxResultSize).collect(Collectors.toList()),
                totalSize));
          }
        });
  }

  private class TableAndSchema {
    private final Table table;
    private final Schema schema;

    public TableAndSchema(Table table, Schema schema) {
      this.table = table;
      this.schema = schema;
    }

    public Table table() {
      return table;
    }

    public Schema schema() {
      return schema;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TableAndSchema that = (TableAndSchema) o;
      return Objects.equals(table, that.table) && Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
      return Objects.hash(table, schema);
    }

    @Override
    public String toString() {
      return "TableAndSchema{" + "table=" + table + ", schema=" + schema + '}';
    }
  }

  @Override
  public Optional<ResultAndTotalSize<List<Table>>> listTablesOfShare(
      String share, int offset, int maxResultSize) {
    return Optional.ofNullable(shares.get(share)).flatMap(shareObj -> {
      var schemaMap = shareObj.schemas();
      var tableList = schemaMap.values().stream()
          .flatMap(x -> x.tables().stream().map(t -> new TableAndSchema(t, x)))
          .collect(Collectors.toList());

      var totalSize = tableList.size();
      if (offset > totalSize) {
        throw new InvalidPageTokenException(
            String.format("Invalid Next Page Token: token %s is larger than totalSize", offset));
      } else {
        return Optional.of(new ResultAndTotalSize<>(
            tableList.stream()
                .skip(offset)
                .limit(maxResultSize)
                .map(t -> t.table)
                .collect(Collectors.toList()),
            totalSize));
      }
    });
  }

  @Override
  public Metastore createMetastore(Metastore metastore) {
    if (metastores.get(metastore.name()) != null) {
      throw new DuplicateKeyException(
          "Metastore with name " + metastore.name() + " already exists");
    } else {
      return metastores.put(metastore.name(), metastore);
    }
  }

  @Override
  public Optional<Metastore> getMetastore(String name) {
    return Optional.ofNullable(metastores.get(name));
  }
}
