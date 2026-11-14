package net.potatocloud.module.rest.service;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.template.Template;
import net.potatocloud.api.template.TemplateCopyOptions;
import net.potatocloud.module.rest.RestServer;
import net.potatocloud.module.rest.auth.Scope;
import net.potatocloud.module.rest.util.FindHelper;
import net.potatocloud.module.rest.util.RequestHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ServiceRoutes {

    private final CloudAPI api;

    public ServiceRoutes(CloudAPI api) {
        this.api = api;
    }

    public void register(RoutesConfig routes) {
        final String path = RestServer.PREFIX + "/services";

        routes.get(path, this::list, Scope.SERVICE_READ);
        routes.get(path + "/{name}", this::get, Scope.SERVICE_READ);
        routes.post(path + "/{name}/stop", this::stop, Scope.SERVICE_STOP);
        routes.post(path + "/{name}/command", this::command, Scope.SERVICE_COMMAND);
        routes.get(path + "/{name}/logs", this::logs, Scope.SERVICE_LOGS);
        routes.post(path + "/{name}/copy", this::copy, Scope.TEMPLATE_COPY);
    }

    private void list(Context ctx) {
        final String group = ctx.queryParam("group");
        final String state = ctx.queryParam("state");

        ctx.json(api.serviceManager().services().stream()
                .filter(service -> group == null || service.group().name().equalsIgnoreCase(group))
                .filter(service -> state == null || service.state().name().equalsIgnoreCase(state))
                .map(ServiceResponse::from)
                .toList());
    }

    private void get(Context ctx) {
        ctx.json(ServiceResponse.from(FindHelper.service(ctx.pathParam("name"))));
    }

    private void stop(Context ctx) {
        api.serviceManager().stop(FindHelper.service(ctx.pathParam("name")));
        ctx.status(202);
    }

    private void command(Context ctx) {
        final Service service = FindHelper.service(ctx.pathParam("name"));
        final CommandRequest request = RequestHelper.read(ctx, CommandRequest.class);

        RequestHelper.require(request.command() != null && !request.command().isBlank(), "command is required");

        api.serviceManager().execute(service, request.command());
        ctx.status(204);
    }

    private void logs(Context ctx) throws Exception {
        final Service service = FindHelper.service(ctx.pathParam("name"));
        final int limit = RequestHelper.intParam(ctx, "lines", 0);

        List<String> lines = api.serviceManager().logs(service).get(10, TimeUnit.SECONDS);
        if (limit > 0 && lines.size() > limit) {
            lines = lines.subList(lines.size() - limit, lines.size());
        }

        ctx.json(new LogsResponse(service.name(), lines));
    }

    private void copy(Context ctx) {
        final Service service = FindHelper.service(ctx.pathParam("name"));
        final CopyRequest request = RequestHelper.read(ctx, CopyRequest.class);

        RequestHelper.require(request.template() != null, "template is required");

        final Template template = FindHelper.template(request.template());
        final String path = request.path() == null ? "" : request.path();

        RequestHelper.require(!path.contains(".."), "path must not contain ..");

        final boolean replace = request.replaceExisting() == null || request.replaceExisting();

        api.serviceManager().copyTo(service, template, new TemplateCopyOptions(path, replace));
        ctx.status(202);
    }
}
