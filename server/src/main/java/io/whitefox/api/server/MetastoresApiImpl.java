package io.whitefox.api.server;

import io.whitefox.api.model.generated.CreateMetastore;
import io.whitefox.api.model.generated.UpdateMetastore;
import io.whitefox.api.server.generated.MetastoresApi;
import io.quarkus.security.identity.SecurityIdentity;
import io.whitefox.api.deltasharing.Mappers;
import io.whitefox.api.model.UpdateMetastore;
import io.whitefox.core.Principal;
import io.whitefox.core.services.MetastoreService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("whitefox-api/v1/metastores")
public class MetastoresApiImpl implements MetastoresApi, ApiUtils {

  private final MetastoreService metastoreService;

  @Inject
  public MetastoresApiImpl(MetastoreService metastoreService) {
    this.metastoreService = metastoreService;
  }

  @Override
  public Response createMetastore(io.whitefox.api.model.CreateMetastore createMetastore) {
    return wrapExceptions(
        () -> Response.status(Response.Status.CREATED)
            .entity(Mappers.metastore2api(metastoreService.createStorageManager(
                Mappers.api2createMetastore(createMetastore, getRequestPrincipal()))))
            .build(),
        exceptionToResponse);
  }

  private Principal getRequestPrincipal() {
    return new Principal("Mr. Fox");
  }

  @Override
  public Response deleteMetastore(String name, String force) {
    Response res = Response.ok().build();
    return res;
  }

  @Override
  public Response describeMetastore(String name) {
    return wrapExceptions(
        () -> optionalToNotFound(
            metastoreService.getMetastore(name),
            metastore -> Response.ok(Mappers.metastore2api(metastore)).build()),
        exceptionToResponse);
  }

  @Override
  public Response listMetastores() {
    Response res = Response.ok().build();
    return res;
  }

  @Override
  public Response updateMetastore(String name, UpdateMetastore updateMetastore) {
    Response res = Response.ok().build();
    return res;
  }

  @Override
  public Response validateMetastore(String name) {
    Response res = Response.ok().build();
    return res;
  }
}
