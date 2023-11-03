package models;

public class AddTableToSchemaInput {
  private final String name;
  private final TableReference reference;

  public AddTableToSchemaInput(String name, TableReference reference) {
    this.name = name;
    this.reference = reference;
  }

  public String getName() {
    return name;
  }

  public TableReference getReference() {
    return reference;
  }

  public static class TableReference {

    private final String providerName;
    private final String name;

    public TableReference(String providerName, String name) {
      this.providerName = providerName;
      this.name = name;
    }

    public String getProviderName() {
      return providerName;
    }

    public String getName() {
      return name;
    }
  }
}
