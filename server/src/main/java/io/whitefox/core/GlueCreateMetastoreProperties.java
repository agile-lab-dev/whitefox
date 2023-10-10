package io.whitefox.core;

public record GlueCreateMetastoreProperties(String catalogId,
                                            AwsCredentials credentials) implements CreateMetastoreProperties {
}
