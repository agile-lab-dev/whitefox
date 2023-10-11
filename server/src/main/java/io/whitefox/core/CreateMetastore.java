package io.whitefox.core;

import java.util.Objects;
import java.util.Optional;

public final class CreateMetastore {
  private final String name;
  private final Optional<String> comment;
  private final MetastoreType type;
  private final CreateMetastoreProperties properties;
  private final Principal currentUser;

  public CreateMetastore(
      String name,
      Optional<String> comment,
      MetastoreType type,
      CreateMetastoreProperties properties,
      Principal currentUser) {
    this.name = name;
    this.comment = comment;
    this.type = type;
    this.properties = properties;
    this.currentUser = currentUser;
  }

  public String name() {
    return name;
  }

  public Optional<String> comment() {
    return comment;
  }

  public MetastoreType type() {
    return type;
  }

  public CreateMetastoreProperties properties() {
    return properties;
  }

  public Principal currentUser() {
    return currentUser;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (CreateMetastore) obj;
    return Objects.equals(this.name, that.name)
        && Objects.equals(this.comment, that.comment)
        && Objects.equals(this.type, that.type)
        && Objects.equals(this.properties, that.properties)
        && Objects.equals(this.currentUser, that.currentUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, comment, type, properties, currentUser);
  }

  @Override
  public String toString() {
    return "CreateMetastore[" + "name="
        + name + ", " + "comment="
        + comment + ", " + "type="
        + type + ", " + "properties="
        + properties + ", " + "currentUser="
        + currentUser + ']';
  }
}
