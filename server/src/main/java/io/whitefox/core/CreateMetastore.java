package io.whitefox.core;

import java.util.Optional;

public record CreateMetastore(String name,
                              Optional<String> comment,
                              MetastoreType type,
                              CreateMetastoreProperties properties,
                              Principal currentUser) {
}
