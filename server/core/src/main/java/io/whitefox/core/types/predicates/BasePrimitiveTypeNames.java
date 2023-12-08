package io.whitefox.core.types.predicates;

public enum BasePrimitiveTypeNames {
    DATE("date"),
    INT("int"),
    FLOAT("float"),
    DOUBLE("double"),
    TIMESTAMP("timestamp"),
    LONG("long"),
    STRING("string")
    ;

    final public String value;

    BasePrimitiveTypeNames(String value) {
        this.value = value;
    }
}
