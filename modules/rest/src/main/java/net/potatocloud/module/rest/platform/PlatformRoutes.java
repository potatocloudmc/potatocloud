package net.potatocloud.module.rest.platform;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformBase;
import net.potatocloud.api.platform.PlatformBuilder;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.module.rest.RestServer;
import net.potatocloud.module.rest.auth.Scope;
import net.potatocloud.module.rest.error.ApiException;
import net.potatocloud.module.rest.util.FindHelper;
import net.potatocloud.module.rest.util.RequestHelper;

import java.util.ArrayList;
import java.util.List;

public final class PlatformRoutes {

    private final CloudAPI api;

    public PlatformRoutes(CloudAPI api) {
        this.api = api;
    }

    public void register(RoutesConfig routes) {
        final String path = RestServer.PREFIX + "/platforms";

        routes.get(path, this::list, Scope.PLATFORM_READ);
        routes.post(path, this::create, Scope.PLATFORM_CREATE);
        routes.get(path + "/{name}", this::get, Scope.PLATFORM_READ);
        routes.delete(path + "/{name}", this::delete, Scope.PLATFORM_DELETE);
        routes.post(path + "/{name}/versions", this::addVersion, Scope.PLATFORM_UPDATE);
        routes.delete(path + "/{name}/versions/{version}", this::removeVersion, Scope.PLATFORM_UPDATE);
    }

    private void list(Context ctx) {
        ctx.json(api.platformManager().platforms().stream().map(PlatformResponse::from).toList());
    }

    private void get(Context ctx) {
        ctx.json(PlatformResponse.from(FindHelper.platform(ctx.pathParam("name"))));
    }

    private void create(Context ctx) {
        final PlatformCreate request = RequestHelper.read(ctx, PlatformCreate.class);

        RequestHelper.requireName(request.name());

        if (api.platformManager().exists(request.name())) {
            throw ApiException.conflict("Platform " + request.name() + " already exists");
        }

        final PlatformBuilder builder = api.platformManager().builder(request.name())
                .custom(true)
                .downloadUrl(request.downloadUrl())
                .base(base(request.base()))
                .preCacheBuilder(request.preCacheBuilder())
                .parser(request.parser())
                .hashType(request.hashType())
                .proxy(request.proxy());

        if (request.prepareSteps() != null) {
            builder.prepareSteps(request.prepareSteps());
        }

        final Platform platform = builder.build();

        api.platformManager().create(platform);
        ctx.status(201).json(PlatformResponse.from(platform));
    }

    private void delete(Context ctx) {
        final Platform platform = FindHelper.platform(ctx.pathParam("name"));

        for (Group group : api.groupManager().groups()) {
            if (group.platform().name().equalsIgnoreCase(platform.name())) {
                throw ApiException.conflict("Platform " + platform.name() + " is used by group " + group.name());
            }
        }

        api.platformManager().delete(platform);
        ctx.status(204);
    }

    private void addVersion(Context ctx) {
        final Platform platform = FindHelper.platform(ctx.pathParam("name"));
        final PlatformVersionCreate request = RequestHelper.read(ctx, PlatformVersionCreate.class);

        RequestHelper.require(request.name() != null && !request.name().isBlank(), "name is required");

        if (platform.hasVersion(request.name())) {
            throw ApiException.conflict("Platform " + platform.name() + " already has version " + request.name());
        }

        final PlatformVersion version = new PlatformVersionImpl(
                platform.name(),
                request.name(),
                request.local(),
                request.downloadUrl(),
                request.fileHash(),
                request.legacy()
        );

        platform.addVersion(version);
        api.platformManager().update(platform);
        ctx.status(201).json(PlatformVersionResponse.from(version));
    }

    private void removeVersion(Context ctx) {
        final Platform platform = FindHelper.platform(ctx.pathParam("name"));
        final String name = ctx.pathParam("version");

        final PlatformVersion version = platform.version(name)
                .orElseThrow(() -> ApiException.notFound("version", name));

        for (Group group : api.groupManager().groups()) {
            if (group.platform().name().equalsIgnoreCase(platform.name()) && group.platformVersion().name().equalsIgnoreCase(version.name())) {
                throw ApiException.conflict("Version " + version.name() + " is used by group " + group.name());
            }
        }

        final List<PlatformVersion> versions = new ArrayList<>(platform.versions());
        versions.remove(version);

        platform.versions(versions);
        api.platformManager().update(platform);
        ctx.status(204);
    }

    private PlatformBase base(String id) {
        final PlatformBase base = PlatformBase.fromId(id);

        RequestHelper.require(id == null || base != PlatformBase.UNKNOWN || id.equalsIgnoreCase("unknown"), "Unknown platform base " + id);
        return base;
    }
}
