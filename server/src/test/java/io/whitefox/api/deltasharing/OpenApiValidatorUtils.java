package io.whitefox.api.deltasharing;

import io.whitefox.OpenApiValidationFilter;
import java.nio.file.Paths;

public interface OpenApiValidatorUtils {

  String specLocation = Paths.get(".")
      .toAbsolutePath()
      .getParent()
      .getParent()
      .resolve("docs/protocol/delta-sharing-protocol-api.yml")
      .toAbsolutePath()
      .toString();

  OpenApiValidationFilter filter = new OpenApiValidationFilter(specLocation);
}
