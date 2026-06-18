package net.potatocloud.webinterface.api.rest;

import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;
import lombok.RequiredArgsConstructor;
import net.potatocloud.webinterface.dto.event.ErrorDto;
import net.potatocloud.webinterface.dto.group.CreateGroupRequestDto;
import net.potatocloud.webinterface.dto.group.GroupDto;
import net.potatocloud.webinterface.dto.group.UpdateGroupRequestDto;
import net.potatocloud.webinterface.service.GroupService;

@RequiredArgsConstructor
public class GroupRestController extends BaseRestController {

    private final GroupService groupService;

    @Override
    public void register() {
        ApiBuilder.path("/api/groups", () -> {
            ApiBuilder.get(this::listGroups);
            ApiBuilder.post(this::createGroup);

            ApiBuilder.get("/{name}", this::getGroupByName);
            ApiBuilder.post("/{name}/start", this::startGroup);
            ApiBuilder.post("/{name}/update", this::updateGroup);
            ApiBuilder.post("/{name}/stopAll", this::stopAllInGroup);
        });
    }

    @OpenApi(
            path = "/api/groups",
            summary = "List all groups",
            description = "Retrieves a list of all available groups",
            tags = {"Groups"},
            responses = {
                    @OpenApiResponse(status = "200", description = "List of groups", content = @OpenApiContent(from = GroupDto.class, type = "array"))
            }
    )
    private void listGroups(io.javalin.http.Context ctx) {
        ctx.json(groupService.getAllGroups());
    }

    @OpenApi(
            path = "/api/groups",
            summary = "Create a new group",
            description = "Creates a new group with the specified configuration",
            tags = {"Groups"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = CreateGroupRequestDto.class)),
            responses = {
                    @OpenApiResponse(status = "201", description = "Group created successfully"),
                    @OpenApiResponse(status = "400", description = "Invalid request", content = @OpenApiContent(from = ErrorDto.class))
            }
    )
    private void createGroup(io.javalin.http.Context ctx) {
        CreateGroupRequestDto request = ctx.bodyAsClass(CreateGroupRequestDto.class);
        if (!groupService.createGroup(request)) {
            ctx.status(HttpStatus.BAD_REQUEST).json(error("Unable to create group"));
            return;
        }
        ctx.status(HttpStatus.CREATED);
    }

    @OpenApi(
            path = "/api/groups/{name}",
            summary = "Get group by name",
            description = "Retrieves a specific group by its name",
            tags = {"Groups"},
            pathParams = @OpenApiParam(name = "name", description = "Group name"),
            responses = {
                    @OpenApiResponse(status = "200", description = "Group details", content = @OpenApiContent(from = GroupDto.class)),
                    @OpenApiResponse(status = "404", description = "Group not found", content = @OpenApiContent(from = ErrorDto.class))
            }
    )
    private void getGroupByName(io.javalin.http.Context ctx) {
        String name = ctx.pathParam("name");
        var group = groupService.getGroupByName(name);
        if (group == null) {
            ctx.status(HttpStatus.NOT_FOUND).json(error("Group '" + name + "' not found"));
            return;
        }
        ctx.json(group);
    }

    @OpenApi(
            path = "/api/groups/{name}/start",
            summary = "Start a group",
            description = "Starts all services in the specified group",
            tags = {"Groups"},
            pathParams = @OpenApiParam(name = "name", description = "Group name"),
            responses = {
                    @OpenApiResponse(status = "204", description = "Group started successfully"),
                    @OpenApiResponse(status = "404", description = "Group not found", content = @OpenApiContent(from = ErrorDto.class))
            }
    )
    private void startGroup(io.javalin.http.Context ctx) {
        String name = ctx.pathParam("name");
        if (!groupService.startGroup(name)) {
            ctx.status(HttpStatus.NOT_FOUND).json(error("Group '" + name + "' not found"));
            return;
        }
        ctx.status(HttpStatus.NO_CONTENT);
    }

    @OpenApi(
            path = "/api/groups/{name}/update",
            summary = "Update a group",
            description = "Updates the configuration of an existing group",
            tags = {"Groups"},
            pathParams = @OpenApiParam(name = "name", description = "Group name"),
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = UpdateGroupRequestDto.class)),
            responses = {
                    @OpenApiResponse(status = "204", description = "Group updated successfully"),
                    @OpenApiResponse(status = "400", description = "Invalid request", content = @OpenApiContent(from = ErrorDto.class)),
                    @OpenApiResponse(status = "404", description = "Group not found", content = @OpenApiContent(from = ErrorDto.class))
            }
    )
    private void updateGroup(io.javalin.http.Context ctx) {
        String name = ctx.pathParam("name");

        if (!groupService.exists(name)) {
            ctx.status(HttpStatus.NOT_FOUND).json(error("Group '" + name + "' not found"));
        }

        UpdateGroupRequestDto dto = ctx.bodyAsClass(UpdateGroupRequestDto.class);

        if (!groupService.updateGroup(dto)) {
            ctx.status(HttpStatus.BAD_REQUEST).json(error("Unable to update group"));
            return;
        }

        ctx.status(HttpStatus.NO_CONTENT);
    }

    @OpenApi(
            path = "/api/groups/{name}/stopAll",
            summary = "Stop all services in group",
            description = "Stops all running services in the specified group",
            tags = {"Groups"},
            pathParams = @OpenApiParam(name = "name", description = "Group name"),
            responses = {
                    @OpenApiResponse(status = "204", description = "All services stopped successfully"),
                    @OpenApiResponse(status = "404", description = "Group not found", content = @OpenApiContent(from = ErrorDto.class))
            }
    )
    private void stopAllInGroup(io.javalin.http.Context ctx) {
        String name = ctx.pathParam("name");
        if (!groupService.stopAllInGroup(name)) {
            ctx.status(HttpStatus.NOT_FOUND).json(error("Group '" + name + "' not found"));
            return;
        }
        ctx.status(HttpStatus.NO_CONTENT);
    }
}
