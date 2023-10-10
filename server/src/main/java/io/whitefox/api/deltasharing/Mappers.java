package io.whitefox.api.deltasharing;

import io.whitefox.core.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Mappers {
  public static io.whitefox.api.deltasharing.model.generated.Share share2api(Share p) {
    return new io.whitefox.api.deltasharing.model.generated.Share().id(p.id()).name(p.name());
  }

  public static io.whitefox.api.deltasharing.model.generated.Schema schema2api(Schema schema) {
    return new io.whitefox.api.deltasharing.model.generated.Schema()
        .name(schema.name())
        .share(schema.share());
  }

  public static io.whitefox.api.deltasharing.model.generated.Table table2api(Table table) {
    return new io.whitefox.api.deltasharing.model.generated.Table()
        .name(table.name())
        .share(table.share())
        .schema(table.schema());
  }

  public static CreateMetastore api2createMetastore(
      io.whitefox.api.model.CreateMetastore createMetastore, Principal principal) {

    return new CreateMetastore(
        createMetastore.getName(),
        Optional.ofNullable(createMetastore.getComment()),
        apitToMetastoreType(createMetastore.getType()),
        api2CreateMetastoreProperties(createMetastore.getProperties(), createMetastore.getType()),
        principal);
  }

  public static CreateMetastoreProperties api2CreateMetastoreProperties(
      io.whitefox.api.model.CreateMetastoreProperties createMetastore,
      io.whitefox.api.model.CreateMetastore.TypeEnum type) {
    return switch (type) {
      case GLUE -> new GlueCreateMetastoreProperties(
          createMetastore.getCatalogId(), api2awsCredentials(createMetastore.getCredentials()));
      default -> throw new IllegalArgumentException("Unknown metastore type " + type.value());
    };
  }

  public static AwsCredentials api2awsCredentials(
      io.whitefox.api.model.SimpleAwsCredentials credentials) {
    return new SimpleAwsCredentials(
        credentials.getAwsAccessKeyId(),
        credentials.getAwsSecretAccessKey(),
        credentials.getRegion());
  }

  public static MetastoreType apitToMetastoreType(
      io.whitefox.api.model.CreateMetastore.TypeEnum type) {
    return switch (type) {
      case GLUE -> MetastoreType.GLUE;
      default -> throw new IllegalArgumentException("Unknown metastore type " + type.value());
    };
  }

  public static <A, B> List<B> mapList(List<A> list, Function<A, B> f) {
    return list.stream().map(f).collect(Collectors.toList());
  }
}
