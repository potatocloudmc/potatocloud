package net.potatocloud.module.rest.group;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupBuilder;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.api.template.Template;
import net.potatocloud.module.rest.RestServer;
import net.potatocloud.module.rest.auth.Scope;
import net.potatocloud.module.rest.error.ApiException;
import net.potatocloud.module.rest.util.FindHelper;
import net.potatocloud.module.rest.util.RequestHelper;

import java.util.List;

public final class GroupRoutes {

    private final CloudAPI api;

    public GroupRoutes(CloudAPI api) {
        this.api = api;
    }

    public void register(RoutesConfig routes) {
        final String path = RestServer.PREFIX + "/groups";

        routes.get(path, this::list, Scope.GROUP_READ);
        routes.post(path, this::create, Scope.GROUP_CREATE);
        routes.get(path + "/{name}", this::get, Scope.GROUP_READ);
        routes.patch(path + "/{name}", this::update, Scope.GROUP_UPDATE);
        routes.delete(path + "/{name}", this::delete, Scope.GROUP_DELETE);
        routes.post(path + "/{name}/start", this::start, Scope.SERVICE_START);
        routes.post(path + "/{name}/stop", this::stop, Scope.SERVICE_STOP);
    }

    private void list(Context ctx) {
        ctx.json(api.groupManager().groups().stream().map(GroupResponse::from).toList());
    }

    private void get(Context ctx) {
        ctx.json(GroupResponse.from(FindHelper.group(ctx.pathParam("name"))));
    }

    private void create(Context ctx) {
        final GroupCreate request = RequestHelper.read(ctx, GroupCreate.class);

        RequestHelper.requireName(request.name());
        RequestHelper.require(request.platform() != null, "platform is required");
        RequestHelper.require(request.platformVersion() != null, "platformVersion is required");

        if (api.groupManager().exists(request.name())) {
            throw ApiException.conflict("Group " + request.name() + " already exists");
        }

        final Platform platform = FindHelper.platform(request.platform());
        final PlatformVersion version = version(platform, request.platformVersion());
        final String node = request.node() == null ? api.clusterManager().localNode().name() : FindHelper.node(request.node()).name();

        final GroupBuilder builder = api.groupManager().builder(request.name())
                .node(node)
                .platform(platform.name())
                .platformVersion(version.name());

        if (request.minServices() != null) {
            builder.minServices(request.minServices());
        }
        if (request.maxServices() != null) {
            builder.maxServices(request.maxServices());
        }
        if (request.maxPlayers() != null) {
            builder.maxPlayers(request.maxPlayers());
        }
        if (request.maxMemory() != null) {
            builder.maxMemory(request.maxMemory());
        }
        if (request.fallback() != null) {
            builder.fallback(request.fallback());
        }
        if (request.maintenance() != null) {
            builder.maintenance(request.maintenance());
        }
        if (request.staticServices() != null) {
            builder.staticServices(request.staticServices());
        }
        if (request.startPriority() != null) {
            builder.startPriority(request.startPriority());
        }
        if (request.startPercentage() != null) {
            builder.startPercentage(request.startPercentage());
        }
        if (request.javaCommand() != null) {
            builder.javaCommand(request.javaCommand());
        }
        if (request.customJvmFlags() != null) {
            builder.customJvmFlags(request.customJvmFlags());
        }
        if (request.templates() != null) {
            request.templates().forEach(builder::template);
        }
        if (request.properties() != null) {
            request.properties().forEach((name, value) -> builder.property(PropertyKey.of(name, value), value));
        }

        final Group group = builder.build();
        validate(group.minServices(), group.maxServices(), group.maxPlayers(), group.maxMemory(), group.startPercentage());

        api.groupManager().create(group);
        ctx.status(201).json(GroupResponse.from(group));
    }

    private void update(Context ctx) {
        final Group group = FindHelper.group(ctx.pathParam("name"));
        final GroupPatch request = RequestHelper.read(ctx, GroupPatch.class);

        validate(
                request.minServices() == null ? group.minServices() : request.minServices(),
                request.maxServices() == null ? group.maxServices() : request.maxServices(),
                request.maxPlayers() == null ? group.maxPlayers() : request.maxPlayers(),
                request.maxMemory() == null ? group.maxMemory() : request.maxMemory(),
                request.startPercentage() == null ? group.startPercentage() : request.startPercentage()
        );

        final List<Template> templates = request.templates() == null ? null : request.templates().stream().map(Template::of).toList();

        if (request.platform() != null || request.platformVersion() != null) {
            final Platform platform = request.platform() == null ? group.platform() : FindHelper.platform(request.platform());
            final String versionName = request.platformVersion() == null ? group.platformVersion().name() : request.platformVersion();

            group.platform(platform, version(platform, versionName));
        }
        if (request.node() != null) {
            group.node(FindHelper.node(request.node()));
        }
        if (request.minServices() != null) {
            group.minServices(request.minServices());
        }
        if (request.maxServices() != null) {
            group.maxServices(request.maxServices());
        }
        if (request.maxPlayers() != null) {
            group.maxPlayers(request.maxPlayers());
        }
        if (request.maxMemory() != null) {
            group.maxMemory(request.maxMemory());
        }
        if (request.fallback() != null) {
            group.fallback(request.fallback());
        }
        if (request.maintenance() != null) {
            group.maintenance(request.maintenance());
        }
        if (request.startPriority() != null) {
            group.startPriority(request.startPriority());
        }
        if (request.startPercentage() != null) {
            group.startPercentage(request.startPercentage());
        }
        if (request.javaCommand() != null) {
            group.javaCommand(request.javaCommand());
        }
        if (request.customJvmFlags() != null) {
            List.copyOf(group.customJvmFlags()).forEach(group::removeCustomJvmFlag);
            request.customJvmFlags().forEach(group::addCustomJvmFlag);
        }
        if (templates != null) {
            List.copyOf(group.templates()).forEach(group::removeTemplate);

            for (Template template : templates) {
                api.templateManager().create(template);
                group.addTemplate(template);
            }
        }

        api.groupManager().update(group);
        ctx.json(GroupResponse.from(group));
    }

    private void delete(Context ctx) {
        api.groupManager().delete(FindHelper.group(ctx.pathParam("name")));
        ctx.status(204);
    }

    private void start(Context ctx) {
        final Group group = FindHelper.group(ctx.pathParam("name"));
        final int count = RequestHelper.intParam(ctx, "count", 1);

        RequestHelper.require(count >= 1, "count must be at least 1");

        if (group.node().isEmpty()) {
            throw ApiException.conflict("Group " + group.name() + " has no node to start on");
        }
        if (group.services().size() + count > group.maxServices()) {
            throw ApiException.conflict("Group " + group.name() + " can only have " + group.maxServices() + " services");
        }

        for (int i = 0; i < count; i++) {
            api.serviceManager().start(group);
        }
        ctx.status(202);
    }

    private void stop(Context ctx) {
        api.serviceManager().stop(FindHelper.group(ctx.pathParam("name")));
        ctx.status(202);
    }

    private PlatformVersion version(Platform platform, String name) {
        return platform.version(name)
                .orElseThrow(() -> ApiException.badRequest("Platform " + platform.name() + " has no version " + name));
    }

    private void validate(int minServices, int maxServices, int maxPlayers, int maxMemory, int startPercentage) {
        RequestHelper.require(minServices >= 0, "minServices cannot be negative");
        RequestHelper.require(maxServices >= minServices, "maxServices cannot be lower than minServices");
        RequestHelper.require(maxPlayers >= 0, "maxPlayers cannot be negative");
        RequestHelper.require(maxMemory > 0, "maxMemory must be greater than 0");
        RequestHelper.require(startPercentage >= 0 && startPercentage <= 100, "startPercentage must be between 0 and 100");
    }
}
