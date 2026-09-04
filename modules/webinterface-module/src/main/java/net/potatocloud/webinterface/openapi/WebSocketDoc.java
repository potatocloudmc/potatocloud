package net.potatocloud.webinterface.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents a Quarkus WebSocket Next endpoint in the generated OpenAPI document,
 * since OpenAPI has no native concept of WebSocket endpoints.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebSocketDoc {

    /**
     * Must match the path used in @WebSocket(path = "...")
     */
    String path();

    String summary();

    String description();

    String[] tags() default {"WebSocket"};
}