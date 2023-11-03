package models;

public class ProviderInput {

  private final String name;
  private final String storageName;
  private final String metastoreName;

  public ProviderInput(String name, String storageName, String metastoreName) {
    this.name = name;
    this.storageName = storageName;
    this.metastoreName = metastoreName;
  }

  public String getName() {
    return name;
  }

  public String getStorageName() {
    return storageName;
  }

  public String getMetastoreName() {
    return metastoreName;
  }
}
