package io.whitefox.api.deltasharing.mapper;

import io.whitefox.api.deltasharing.model.DeltaTableMetadata;
import io.whitefox.api.deltasharing.server.restdto.TableResponseMetadata;

public interface TableMetadataMapper {

  TableResponseMetadata toTableResponseMetadata(DeltaTableMetadata deltaTableMetadata);
}
