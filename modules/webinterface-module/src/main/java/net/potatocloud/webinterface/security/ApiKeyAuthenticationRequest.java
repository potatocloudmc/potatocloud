package net.potatocloud.webinterface.security;

import io.quarkus.security.identity.request.BaseAuthenticationRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Authentication request wrapping a raw API key value extracted from
 * either the X-API-Key header (REST calls) or the apiKey query param
 * (WebSocket handshake, since browsers cannot set custom headers on
 * the WebSocket upgrade request).
 */
@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class ApiKeyAuthenticationRequest extends BaseAuthenticationRequest {

    private final String apiKey;

}
