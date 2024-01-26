package io.whitefox.api.server;

import io.whitefox.core.services.DeltaSharingCapabilities;

public interface DeltaHeaders {
  String DELTA_SHARING_RESPONSE_FORMAT = DeltaSharingCapabilities.DELTA_SHARING_RESPONSE_FORMAT;
  String DELTA_TABLE_VERSION_HEADER = "Delta-Table-Version";
}
