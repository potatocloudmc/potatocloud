package net.potatocloud.module.rest.property;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.module.rest.RestServer;
import net.potatocloud.module.rest.auth.Scope;
import net.potatocloud.module.rest.error.ApiException;
import net.potatocloud.module.rest.util.FindHelper;
import net.potatocloud.module.rest.util.RequestHelper;

import java.util.function.Consumer;
import java.util.function.Function;

public final class PropertyRoutes {

    private final CloudAPI api;

    public PropertyRoutes(CloudAPI api) {
        this.api = api;
    }

    public void register(RoutesConfig routes) {
        register(routes, "", _ -> api.globalProperties(), _ -> {});
        register(routes, "/groups/{name}", ctx -> FindHelper.group(ctx.pathParam("name")), api.groupManager()::update);
        register(routes, "/services/{name}", ctx -> FindHelper.service(ctx.pathParam("name")), api.serviceManager()::update);
        register(routes, "/players/{player}", ctx -> FindHelper.player(ctx.pathParam("player")), api.playerManager()::update);
    }

    private <T extends PropertyHolder> void register(RoutesConfig routes, String owner, Function<Context, T> source, Consumer<T> saver) {
        final String path = RestServer.PREFIX + owner + "/properties";

        routes.get(path, ctx -> ctx.json(PropertyResponse.map(source.apply(ctx))), Scope.PROPERTY_READ);

        routes.get(path + "/{key}", ctx -> {
            final T holder = source.apply(ctx);
            final PropertyKey<?> key = key(holder, ctx.pathParam("key"));

            ctx.json(new PropertyResponse(key.name(), holder.properties().get(key)));
        }, Scope.PROPERTY_READ);

        routes.put(path + "/{key}", ctx -> {
            final T holder = source.apply(ctx);
            final String name = ctx.pathParam("key");
            final Object value = RequestHelper.read(ctx, PropertyValue.class).value();

            RequestHelper.require(value instanceof String || value instanceof Number || value instanceof Boolean, "value must be a string, number or boolean");

            final PropertyKey<Object> key = keyFor(holder, name, value);
            final Object fallback = key.defaultValue();

            RequestHelper.require(fallback == null || fallback.getClass().isInstance(value), "Property " + name + " expects a " + (fallback == null ? "" : fallback.getClass().getSimpleName()));

            holder.set(key, value);
            saver.accept(holder);
            ctx.json(new PropertyResponse(name, value));
        }, Scope.PROPERTY_WRITE);

        routes.delete(path + "/{key}", ctx -> {
            final T holder = source.apply(ctx);

            holder.removeProperty(key(holder, ctx.pathParam("key")));
            saver.accept(holder);
            ctx.status(204);
        }, Scope.PROPERTY_WRITE);
    }

    private PropertyKey<?> key(PropertyHolder holder, String name) {
        return holder.get(name).orElseThrow(() -> ApiException.notFound("property", name));
    }

    @SuppressWarnings("unchecked")
    private PropertyKey<Object> keyFor(PropertyHolder holder, String name, Object value) {
        final PropertyKey<?> key = holder.get(name)
                .or(() -> DefaultProperties.values().stream().filter(known -> known.name().equals(name)).findFirst())
                .orElseGet(() -> PropertyKey.of(name, value));

        return (PropertyKey<Object>) key;
    }
}
