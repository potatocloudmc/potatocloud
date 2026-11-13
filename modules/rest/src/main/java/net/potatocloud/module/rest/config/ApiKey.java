package net.potatocloud.module.rest.config;

import net.potatocloud.module.rest.auth.Scope;

import java.util.List;

public record ApiKey(String name, String key, List<String> scopes) {

    public ApiKey {
        scopes = scopes == null ? List.of() : List.copyOf(scopes);
    }

    public boolean allows(Scope scope) {
        final String area = scope.id().split("\\.")[0];

        return scopes.contains("*") || scopes.contains(area + ".*") || scopes.contains(scope.id());
    }
}
