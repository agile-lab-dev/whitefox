package io.whitefox.api.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.All;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import java.util.List;

public class JsonConfiguration {

  // Replaces the CDI producer for ObjectMapper built into Quarkus
  @ApplicationScoped
  @Produces
  ObjectMapper objectMapper(@All List<ObjectMapperCustomizer> customizers) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    // Apply all ObjectMapperCustomizer beans (incl. Quarkus)
    for (ObjectMapperCustomizer customizer : customizers) {
      customizer.customize(mapper);
    }

    return mapper;
  }
}
