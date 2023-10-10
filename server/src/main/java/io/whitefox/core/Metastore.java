package io.whitefox.core;

import java.util.Optional;

public record Metastore(String name,
                        Optional<String> comment,
                        Principal owner,
                        MetastoreType type,
                        CreateMetastoreProperties properties,
                        Long validatedAt,
                        Long createdAt,
                        Principal createdBy,
                        Long updatedAt,
                        Principal updatedBy) {

}
