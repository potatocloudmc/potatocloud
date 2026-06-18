package net.potatocloud.webinterface.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.net.URI;

@Path("/docs")
public class DocsController {

    @Operation(hidden = true)
    @GET
    public Response redirectToSlash() {
        return Response.seeOther(URI.create("/docs/")).build();
    }

}
