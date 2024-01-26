package io.whitefox.api.deltasharing;

import io.whitefox.api.deltasharing.model.v1.generated.*;
import io.whitefox.api.server.CommonMappers;
import io.whitefox.core.*;
import io.whitefox.core.Schema;
import io.whitefox.core.Share;
import io.whitefox.core.delta.Metadata;
import io.whitefox.core.delta.Protocol;
import io.whitefox.core.results.ReadTableResult;
import io.whitefox.core.services.DeltaSharingCapabilities;
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

  private static ParquetMetadataObject metadata2Api(Metadata metadata) {
    return new ParquetMetadataObject()
        .metaData(new ParquetMetadataObjectMetaData()
            .numFiles(metadata.numFiles().orElse(null))
            .version(metadata.version())
            .size(metadata.size().orElse(null))
            .id(metadata.id())
            .name(metadata.name().orElse(null))
            .description(metadata.description().orElse(null))
            .format(new ParquetFormatObject().provider(metadata.format().provider()))
            .schemaString(metadata.tableSchema().structType().toJson())
            .partitionColumns(metadata.partitionColumns())
            ._configuration(metadata.configuration()));
  }

  private static DeltaProtocolObject protocol2Api(Protocol protocol) {
    return new DeltaProtocolObject()
        .protocol(new DeltaProtocolObjectProtocol()
            .deltaProtocol(new DeltaProtocolObjectProtocolDeltaProtocol()
                .minReaderVersion(protocol.minReaderVersion().orElse(1))
                .minWriterVersion(protocol.minWriterVersion().orElse(1))));
  }

  private static DeltaFileObject file2Api(TableFile f) {
    return new DeltaFileObject()
        .id(f.id())
        .version(f.version().orElse(null))
        .deletionVectorFileId(null) // TODO
        .timestamp(f.timestamp().orElse(null))
        .expirationTimestamp(f.expirationTimestamp())
        .deltaSingleAction(new DeltaSingleAction()
            ._file(new DeltaAddFileAction()
                .id(f.id())
                .url(f.url())
                .partitionValues(f.partitionValues())
                .size(f.size())
                .stats(f.stats().orElse(null))
                .version(f.version().orElse(null))
                .timestamp(f.timestamp().orElse(null))
                .expirationTimestamp(f.expirationTimestamp())));
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

  public static TableMetadataResponseObject toTableResponseMetadata(Metadata m) {
    return new TableMetadataResponseObject()
        .protocol(new ParquetProtocolObject()
            .protocol(new ParquetProtocolObjectProtocol().minReaderVersion(1)))
        .metadata(metadata2Api(m));
  }

  public static String toCapabilitiesHeader(DeltaSharingCapabilities deltaSharingCapabilities) {
    return deltaSharingCapabilities.values().entrySet().stream()
        .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
        .collect(Collectors.joining(";"));
  }
}
