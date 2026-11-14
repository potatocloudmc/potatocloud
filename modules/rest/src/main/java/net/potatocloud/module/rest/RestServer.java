package net.potatocloud.module.rest;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson3;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.module.rest.auth.AuthHandler;
import net.potatocloud.module.rest.config.RestConfig;
import net.potatocloud.module.rest.error.ErrorHandler;
import net.potatocloud.module.rest.group.GroupRoutes;
import net.potatocloud.module.rest.node.NodeRoutes;
import net.potatocloud.module.rest.platform.PlatformRoutes;
import net.potatocloud.module.rest.player.PlayerRoutes;
import net.potatocloud.module.rest.property.PropertyRoutes;
import net.potatocloud.module.rest.service.ServiceRoutes;
import net.potatocloud.module.rest.template.TemplateRoutes;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

public final class RestServer {

    public static final String PREFIX = "/api/v1";

    private final CloudAPI api;
    private final RestConfig config;
    private Javalin app;

    public RestServer(CloudAPI api, RestConfig config) {
        this.api = api;
        this.config = config;
    }

    public void start() {
        // jetty looks up some of its classes through the context class loader
        final Thread thread = Thread.currentThread();
        final ClassLoader previous = thread.getContextClassLoader();
        thread.setContextClassLoader(getClass().getClassLoader());

        try {
            app = Javalin.create(this::configure);
            app.start(config.host(), config.port());
        } finally {
            thread.setContextClassLoader(previous);
        }
    }

    public void stop() {
        if (app != null) {
            app.stop();
            app = null;
        }
    }

    private void configure(JavalinConfig javalin) {
        javalin.startup.showJavalinBanner = false;
        javalin.startup.showOldJavalinVersionWarning = false;
        javalin.concurrency.useVirtualThreads = true;
        javalin.jsonMapper(new JavalinJackson3(JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build(), true));

        if (!config.corsOrigins().isEmpty()) {
            javalin.bundledPlugins.enableCors(cors -> cors.addRule(rule -> {
                if (config.corsOrigins().contains("*")) {
                    rule.anyHost();
                    return;
                }
                config.corsOrigins().forEach(rule::allowHost);
            }));
        }

        javalin.routes.beforeMatched(new AuthHandler(config));
        new ErrorHandler(api.logger()).register(javalin.routes);

        new GroupRoutes(api).register(javalin.routes);
        new ServiceRoutes(api).register(javalin.routes);
        new PlatformRoutes(api).register(javalin.routes);
        new PlayerRoutes(api).register(javalin.routes);
        new NodeRoutes(api).register(javalin.routes);
        new TemplateRoutes(api).register(javalin.routes);
        new PropertyRoutes(api).register(javalin.routes);
    }
}
