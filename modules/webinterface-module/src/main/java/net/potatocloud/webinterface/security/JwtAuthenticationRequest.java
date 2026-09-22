package net.potatocloud.webinterface.security;

import io.quarkus.security.identity.request.BaseAuthenticationRequest;

public class JwtAuthenticationRequest extends BaseAuthenticationRequest {
    private final String token;

    public JwtAuthenticationRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}