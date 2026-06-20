package net.potatocloud.webinterface.openapi;

import net.potatocloud.webinterface.websocket.*;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;

import java.util.List;

public class WebSocketDocFilter implements OASFilter {

    private static final List<Class<?>> WEBSOCKET_ENDPOINTS = List.of(
            PlayersSocket.class, ServicesSocket.class, ServiceStatsSocket.class, ScreenLogSocket.class, ServiceDetailsSocket.class
    );

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        for (Class<?> endpointClass : WEBSOCKET_ENDPOINTS) {
            WebSocketDoc doc = endpointClass.getAnnotation(WebSocketDoc.class);
            if (doc == null) {
                continue;
            }
            openAPI.getPaths().addPathItem(doc.path(), toPathItem(doc));
        }
    }


    private PathItem toPathItem(WebSocketDoc doc) {
        Operation operation = OASFactory.createOperation()
                .summary(doc.summary())
                .description(doc.description())
                .responses(OASFactory.createAPIResponses()
                        .addAPIResponse("101", OASFactory.createAPIResponse()
                                .description("Switching Protocols - WebSocket upgrade successful")));

        for (String tag : doc.tags()) {
            operation.addTag(tag);
        }

        PathItem pathItem = OASFactory.createPathItem();
        pathItem.setGET(operation);
        return pathItem;
    }

}
