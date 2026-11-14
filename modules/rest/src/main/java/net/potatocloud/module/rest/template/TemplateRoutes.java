package net.potatocloud.module.rest.template;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.template.Template;
import net.potatocloud.module.rest.RestServer;
import net.potatocloud.module.rest.auth.Scope;
import net.potatocloud.module.rest.error.ApiException;
import net.potatocloud.module.rest.util.FindHelper;
import net.potatocloud.module.rest.util.RequestHelper;

public final class TemplateRoutes {

    private final CloudAPI api;

    public TemplateRoutes(CloudAPI api) {
        this.api = api;
    }

    public void register(RoutesConfig routes) {
        final String path = RestServer.PREFIX + "/templates";

        routes.get(path, this::list, Scope.TEMPLATE_READ);
        routes.post(path, this::create, Scope.TEMPLATE_CREATE);
        routes.delete(path + "/{name}", this::delete, Scope.TEMPLATE_DELETE);
    }

    private void list(Context ctx) {
        ctx.json(api.templateManager().templates().stream().map(TemplateResponse::from).toList());
    }

    private void create(Context ctx) {
        final TemplateCreate request = RequestHelper.read(ctx, TemplateCreate.class);

        RequestHelper.require(request.name() != null, "name is required");

        final Template template = Template.of(request.name());

        if (api.templateManager().exists(template.name())) {
            throw ApiException.conflict("Template " + template.name() + " already exists");
        }

        api.templateManager().create(template);
        ctx.status(201).json(TemplateResponse.from(template));
    }

    private void delete(Context ctx) {
        final Template template = FindHelper.template(ctx.pathParam("name"));

        for (Group group : api.groupManager().groups()) {
            if (group.templates().contains(template)) {
                throw ApiException.conflict("Template " + template.name() + " is used by group " + group.name());
            }
        }

        api.templateManager().delete(template);
        ctx.status(204);
    }
}
