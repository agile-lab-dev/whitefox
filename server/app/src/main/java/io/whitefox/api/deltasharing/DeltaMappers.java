package io.whitefox.api.deltasharing;

import io.whitefox.api.deltasharing.model.v1.generated.*;
import io.whitefox.api.server.CommonMappers;
import io.whitefox.core.*;
import io.whitefox.core.Schema;
import io.whitefox.core.Share;
import io.whitefox.core.services.capabilities.ResponseFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DeltaMappers {

  public static io.whitefox.api.deltasharing.model.v1.generated.Share share2api(Share p) {
    return new io.whitefox.api.deltasharing.model.v1.generated.Share()
        .id(p.id())
        .name(p.name());
  }

  public static io.whitefox.api.deltasharing.model.v1.generated.Schema schema2api(Schema schema) {
    return new io.whitefox.api.deltasharing.model.v1.generated.Schema()
        .name(schema.name())
        .share(schema.share());
  }

  public static ReadTableRequest api2ReadTableRequest(QueryRequest request) {
    if (request.getStartingVersion() != null && request.getEndingVersion() != null) {
      throw new IllegalArgumentException("The startingVersion and endingVersion are not supported");
    } else if (request.getStartingVersion() != null) {
      throw new IllegalArgumentException("The startingVersion is not supported");
    } else if (request.getEndingVersion() != null) {
      throw new IllegalArgumentException("The endingVersion is not supported");
    } else if (request.getVersion() != null && request.getVersion() < 0) {
      throw new IllegalArgumentException("version cannot be negative.");
    } else if (request.getVersion() != null && request.getTimestamp() == null) {
      return new ReadTableRequest.ReadTableVersion(
          request.getPredicateHints(),
          Optional.ofNullable(request.getLimitHint()),
          request.getVersion());
    } else if (request.getVersion() == null && request.getTimestamp() != null) {
      return new ReadTableRequest.ReadTableAsOfTimestamp(
          request.getPredicateHints(),
          Optional.ofNullable(request.getLimitHint()),
          CommonMappers.parseTimestamp(request.getTimestamp()));
    } else if (request.getVersion() == null && request.getTimestamp() == null) {
      return new ReadTableRequest.ReadTableCurrentVersion(
          request.getPredicateHints(), Optional.ofNullable(request.getLimitHint()));
    } else {
      throw new IllegalArgumentException("Cannot specify both version and timestamp");
    }
  }

  public static TableQueryResponseObject readTableResult2api(ReadTableResult readTableResult) {
    return new TableQueryResponseObject()
        .metadata(metadata2Api(readTableResult.metadata()))
        .protocol(protocol2Api(readTableResult.protocol()))
        .files(readTableResult.files().stream()
            .map(DeltaMappers::file2Api)
            .collect(Collectors.toList()));
  }

  private static MetadataObject metadata2Api(Metadata metadata) {
    return new MetadataObject()
        .metaData(new MetadataObjectMetaData()
            .id(metadata.id())
            .name(metadata.name().orElse(null))
            .description(metadata.description().orElse(null))
            .format(new FormatObject()
                .provider("parquet")) // this is hard-coded because we don't support delta yet
            .schemaString(metadata.tableSchema().structType().toJson())
            .partitionColumns(metadata.partitionColumns())
            ._configuration(metadata.configuration())
            .version(metadata.version().orElse(null))
            .numFiles(metadata.numFiles().orElse(null)));
  }

  private static ProtocolObject protocol2Api(Protocol protocol) {
    return new ProtocolObject()
        .protocol(new ProtocolObjectProtocol()
            .minReaderVersion(protocol.minReaderVersion().orElse(1)));
  }

  private static FileObject file2Api(TableFile f) {
    return new FileObject()
        ._file(new FileObjectFile()
            .id(f.id())
            .url(f.url())
            .partitionValues(f.partitionValues())
            .size(f.size())
            .stats(f.stats().orElse(null))
            .version(f.version().orElse(null))
            .timestamp(f.timestamp().orElse(null))
            .expirationTimestamp(f.expirationTimestamp()));
  }

  public static TableReferenceAndReadRequest api2TableReferenceAndReadRequest(
      QueryRequest request, String share, String schema, String table) {
    return new TableReferenceAndReadRequest(share, schema, table, api2ReadTableRequest(request));
  }

  public static io.whitefox.api.deltasharing.model.v1.generated.Table table2api(
      SharedTable sharedTable) {
    return new io.whitefox.api.deltasharing.model.v1.generated.Table()
        .name(sharedTable.name())
        .share(sharedTable.share())
        .schema(sharedTable.schema());
  }

  /**
   * Serializes the response format in its text-based representation
   */
  public static String toResponseFormatHeader(ResponseFormat responseFormat) {
    return responseFormat.stringRepresentation();
  }

  public static TableMetadataResponseObject toTableResponseMetadata(Metadata m) {
    return new TableMetadataResponseObject()
        .protocol(new ProtocolObject().protocol(new ProtocolObjectProtocol().minReaderVersion(1)))
        .metadata(metadata2Api(m));
  }
}
