package io.whitefox.api.deltasharing.mapper;

import io.whitefox.api.deltasharing.model.DeltaTableMetadata;
import io.whitefox.api.deltasharing.model.generated.*;
import io.whitefox.api.deltasharing.server.restdto.TableResponseMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;

@ApplicationScoped
public class TableMetadataMapperImpl implements TableMetadataMapper {

  public TableResponseMetadata toTableResponseMetadata(DeltaTableMetadata deltaTableMetadata) {
    return new TableResponseMetadata(
        new ProtocolResponse()
            .protocol(new ProtocolResponseProtocol().minReaderVersion(new BigDecimal(1))),
        new MetadataResponse()
            .metadata(new MetadataResponseMetadata()
                .id(deltaTableMetadata.getMetadata().getId())
                .format(new MetadataResponseMetadataFormat()
                    .provider(deltaTableMetadata.getMetadata().getFormat().getProvider()))
                .schemaString(deltaTableMetadata.getMetadata().getSchema().toJson())
                .partitionColumns(deltaTableMetadata.getMetadata().getPartitionColumns())));
  }
}
