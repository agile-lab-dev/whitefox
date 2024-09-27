package io.whitefox.api.server;

import io.quarkus.runtime.util.ExceptionUtil;
import io.whitefox.api.deltasharing.model.v1.generated.CommonErrorResponse;
import io.whitefox.core.Principal;
import io.whitefox.core.services.exceptions.AlreadyExists;
import io.whitefox.core.services.exceptions.NotFound;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jboss.logging.Logger;

public interface ApiUtils extends DeltaHeaders {

  static final Logger logger = Logger.getLogger(ApiUtils.class);

  Function<Throwable, Response> exceptionToResponse = t -> {
    logger.warn(t.getMessage(), t);
    if (t instanceof IllegalArgumentException) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new CommonErrorResponse()
              .errorCode("BAD REQUEST")
              .message(ExceptionUtil.generateStackTrace(t)))
          .build();
    } else if (t instanceof AlreadyExists) {
      return Response.status(Response.Status.CONFLICT)
          .entity(new CommonErrorResponse()
              .errorCode("CONFLICT")
              .message(ExceptionUtil.generateStackTrace(t)))
          .build();
    } else if (t instanceof NotFound) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new CommonErrorResponse()
              .errorCode("NOT FOUND")
              .message(ExceptionUtil.generateStackTrace(t)))
          .build();
    } else if (t instanceof DateTimeParseException) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new CommonErrorResponse()
              .errorCode("BAD REQUEST - timestamp provided is not formatted correctly")
              .message(ExceptionUtil.generateStackTrace(t)))
          .build();
    } else if (t instanceof ForbiddenException) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(new CommonErrorResponse()
              .errorCode("FORBIDDEN ACCESS")
              .message(ExceptionUtil.generateStackTrace(t)))
          .build();
    } else {
      return Response.status(Response.Status.BAD_GATEWAY)
          .entity(new CommonErrorResponse()
              .errorCode("BAD GATEWAY")
              .message(ExceptionUtil.generateStackTrace(t)))
          .build();
    }
  };

  default Response wrapExceptions(Supplier<Response> f, Function<Throwable, Response> mapper) {
    try {
      return f.get();
    } catch (Throwable t) {
      return mapper.apply(t);
    }
  }

  default Response notFoundResponse() {
    return Response.status(Response.Status.NOT_FOUND)
        .entity(new CommonErrorResponse().errorCode("1").message("NOT FOUND"))
        .build();
  }

  default Response forbiddenResponse() {
    return Response.status(Response.Status.FORBIDDEN)
        .entity(new CommonErrorResponse().errorCode("2").message("UNAUTHORIZED ACCESS"))
        .build();
  }

  default <T> Response optionalToNotFound(Optional<T> opt, Function<T, Response> fn) {
    return opt.map(fn).orElse(notFoundResponse());
  }

  default Principal getRequestPrincipal() {
    return new Principal("Mr. Fox");
  }

  default Principal resolvePrincipal(String s) {
    return new Principal(s);
  }

  default Optional<Timestamp> parseTimestamp(String timestamp) {
    return Optional.ofNullable(timestamp)
        .map(ts -> new Timestamp(OffsetDateTime.parse(ts, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .toInstant()
            .toEpochMilli()));
  }
}
