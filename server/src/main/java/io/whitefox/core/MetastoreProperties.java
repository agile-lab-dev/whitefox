package io.whitefox.core;

import java.util.Objects;

public interface MetastoreProperties {
  final class GlueMetastoreProperties implements MetastoreProperties {
    private final String catalogId;
    private final AwsCredentials credentials;

    public GlueMetastoreProperties(String catalogId, AwsCredentials credentials) {
      this.catalogId = catalogId;
      this.credentials = credentials;
    }

    public String catalogId() {
      return catalogId;
    }

    public AwsCredentials credentials() {
      return credentials;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null || obj.getClass() != this.getClass()) return false;
      var that = (GlueMetastoreProperties) obj;
      return Objects.equals(this.catalogId, that.catalogId)
          && Objects.equals(this.credentials, that.credentials);
    }

    @Override
    public int hashCode() {
      return Objects.hash(catalogId, credentials);
    }

    @Override
    public String toString() {
      return "GlueMetastoreProperties[" + "catalogId="
          + catalogId + ", " + "credentials="
          + credentials + ']';
    }
  }
}
