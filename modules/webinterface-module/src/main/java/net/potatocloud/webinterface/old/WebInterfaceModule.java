package net.potatocloud.webinterface.old;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import lombok.Getter;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.module.AbstractModule;
import net.potatocloud.common.config.Config;
import net.potatocloud.common.config.yaml.YamlConfig;
import net.potatocloud.node.Node;
import net.potatocloud.webinterface.old.api.rest.*;
import net.potatocloud.webinterface.old.api.websocket.handler.*;
import net.potatocloud.webinterface.old.api.websocket.handler.group.GroupsWebSocketHandler;
import net.potatocloud.webinterface.old.api.websocket.session.WebSocketSessionManager;
import net.potatocloud.webinterface.old.dto.event.ErrorDto;
import net.potatocloud.webinterface.old.security.AuthService;
import net.potatocloud.webinterface.old.service.*;
import net.potatocloud.webinterface.old.service.broadcast.PlayerBroadcastService;
import net.potatocloud.webinterface.old.service.broadcast.ScreenLogBroadcastService;
import net.potatocloud.webinterface.old.service.broadcast.ServerBroadcastService;
import net.potatocloud.webinterface.old.service.broadcast.ServiceDetailsBroadcastService;
import net.potatocloud.webinterface.old.service.broadcast.group.GroupDetailsBroadcastService;
import net.potatocloud.webinterface.old.service.broadcast.group.GroupsBroadcastService;
import net.potatocloud.webinterface.old.service.broadcast.stats.StatsServicesBroadcastService;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.nio.file.Path;

public class WebInterfaceModule extends AbstractModule {

    @Getter
    public static WebInterfaceModule instance;
    @Getter
    public CloudAPI cloudAPI;
    private Javalin app;
    private WebSocketSessionManager sessionManager;

    private GroupDetailsBroadcastService groupDetailsBroadcastService;
    private ScreenLogBroadcastService screenLogBroadcastService;
    private PlayerBroadcastService playerBroadcastService;
    private GroupsBroadcastService groupsBroadcastService;
    private StatsServicesBroadcastService statsServicesBroadcastService;
    private ServerBroadcastService serverBroadcastService;
    private ServiceDetailsBroadcastService serviceDetailsBroadcastService;

    // todo: rewrite on quarkus-app from Development

    @Override
    public void onEnable() {
        instance = this;
        cloudAPI = CloudAPI.instance();
        Node node = Node.getInstance();

        Config config = new YamlConfig(Path.of("modules", "webinterface").resolve("wi-config.yml"), getClass().getClassLoader());
        config.load();
        AuthService authService = new AuthService(config);

        NodeService nodeService = new NodeService(cloudAPI, node);
        GroupService groupService = new GroupService(cloudAPI, node);
        PlayerService playerService = new PlayerService(cloudAPI, node);
        PlatformService platformService = new PlatformService(cloudAPI);
        StatsService statsService = new StatsService(cloudAPI);
        ServerService serverService = new ServerService(cloudAPI);

        statsService.start();

        int wsUpdateIntervalSeconds = config.get("ws-update-interval-seconds").asInt();
        int wsPingIntervalSeconds = config.get("ws-ping-interval-seconds").asInt();

        sessionManager = new WebSocketSessionManager();
        groupDetailsBroadcastService = new GroupDetailsBroadcastService(groupService, wsUpdateIntervalSeconds);
        groupDetailsBroadcastService.start();

        screenLogBroadcastService = new ScreenLogBroadcastService(node);

        playerBroadcastService = new PlayerBroadcastService(cloudAPI, playerService, wsUpdateIntervalSeconds);
        playerBroadcastService.registerCloudListeners();
        playerBroadcastService.start();

        groupsBroadcastService = new GroupsBroadcastService(cloudAPI, groupService, wsUpdateIntervalSeconds);
        groupsBroadcastService.registerCloudListeners();
        groupsBroadcastService.start();

        statsServicesBroadcastService = new StatsServicesBroadcastService(cloudAPI, statsService, wsUpdateIntervalSeconds);
        statsServicesBroadcastService.registerCloudListeners();
        statsServicesBroadcastService.start();

        serverBroadcastService = new ServerBroadcastService(cloudAPI, serverService, wsUpdateIntervalSeconds);
        serverBroadcastService.registerCloudListeners();
        serverBroadcastService.start();

        serviceDetailsBroadcastService = new ServiceDetailsBroadcastService(serverService, wsUpdateIntervalSeconds);
        serviceDetailsBroadcastService.start();

        GroupRestController groupRestController = new GroupRestController(groupService);
        PlatformRestController platformRestController = new PlatformRestController(platformService);
        PlayerRestController playerRestController = new PlayerRestController(playerService);
        StatsRestController statsRestController = new StatsRestController(statsService);
        ScreenRestController screenRestController = new ScreenRestController(nodeService);
        ServiceRestController serviceRestController = new ServiceRestController(serverService);

        GroupWebSocketHandler groupWebSocketHandler = new GroupWebSocketHandler(
                groupService, sessionManager, authService, wsPingIntervalSeconds
        );
        PlayerWebSocketHandler playerWebSocketHandler = new PlayerWebSocketHandler(
                playerService, sessionManager, authService, wsPingIntervalSeconds
        );
        PlayerLiveWebSocketHandler playerLiveWebSocketHandler = new PlayerLiveWebSocketHandler(
                sessionManager, playerBroadcastService, authService, wsPingIntervalSeconds
        );
        GroupDetailsWebSocketHandler groupDetailsWebSocketHandler = new GroupDetailsWebSocketHandler(
                groupService, groupDetailsBroadcastService, sessionManager, authService, wsPingIntervalSeconds
        );
        GroupsWebSocketHandler groupsWebSocketHandler = new GroupsWebSocketHandler(
                sessionManager, groupsBroadcastService, authService, wsPingIntervalSeconds
        );
        ScreenWebSocketHandler screenWebSocketHandler = new ScreenWebSocketHandler(
                nodeService, screenLogBroadcastService, sessionManager, authService, wsPingIntervalSeconds
        );
        StatsServiceWebSocketHandler statsServiceWebSocketHandler = new StatsServiceWebSocketHandler(
                statsServicesBroadcastService, sessionManager, authService, wsUpdateIntervalSeconds
        );
        ServicesWebSocketHandler servicesWebSocketHandler = new ServicesWebSocketHandler(
                serverBroadcastService, sessionManager, authService, wsUpdateIntervalSeconds
        );
        ServiceDetailsWebSocketHandler serviceDetailsWebSocketHandler = new ServiceDetailsWebSocketHandler(
                serverService, serviceDetailsBroadcastService, sessionManager, authService, wsUpdateIntervalSeconds
        );

        app = Javalin.create(cfg -> {
            cfg.jetty.threadPool = new QueuedThreadPool(500, 8, 60000);

            cfg.jsonMapper(new JavalinJackson());

            cfg.bundledPlugins.enableCors(plugin -> plugin.addRule(CorsPluginConfig.CorsRule::anyHost));
            cfg.registerPlugin(new OpenApiPlugin(openApiPluginConfiguration -> {
                openApiPluginConfiguration.withDefinitionConfiguration((_, openApiSchemaBuilder) -> {
                    openApiSchemaBuilder.info(info -> {
                        info.title("PotatoCloud WebInterface API");
                        info.version("1.0.0");
                        info.description("REST and WebSocket API for PotatoCloud management and monitoring");
                        info.contact("Fedox", "https://fedox.ovh", "f3dox@proton.me");
                    });
                });
            }));

            cfg.routes.before("/api/*", authService::authorizeHttp);

            cfg.routes.apiBuilder(() -> {
                groupRestController.register();
                platformRestController.register();
                playerRestController.register();
                statsRestController.register();
                screenRestController.register();
                serviceRestController.register();
            });

            cfg.routes.ws("/ws/stats/services", statsServiceWebSocketHandler::configure);
            cfg.routes.ws("/ws/services", servicesWebSocketHandler::configure);
            cfg.routes.ws("/ws/group-details", groupDetailsWebSocketHandler::configure);
            cfg.routes.ws("/ws/groups", groupsWebSocketHandler::configure);
            cfg.routes.ws("/ws/players", playerWebSocketHandler::configure);
            cfg.routes.ws("/ws/players/live", playerLiveWebSocketHandler::configure);
            cfg.routes.ws("/ws/screens/{name}", screenWebSocketHandler::configure);
            cfg.routes.ws("/ws/service", serviceDetailsWebSocketHandler::configure);

            cfg.routes.exception(Exception.class, (exception, ctx) -> {
                cloudAPI.logger().exception(exception);
                ctx.status(500).json(ErrorDto.builder().error("Internal server error").build());
            });
        });

        app.start(config.get("bind-address").asString(), config.get("port").asInt());

        cloudAPI.logger().info("WebInterface API running on " + config.get("bind-address").asString() + ":" + config.get("port").asInt());
    }

    @Override
    public void onDisable() {
        if (groupDetailsBroadcastService != null) {
            groupDetailsBroadcastService.shutdown();
        }

        if (screenLogBroadcastService != null) {
            screenLogBroadcastService.shutdown();
        }

        if (playerBroadcastService != null) {
            playerBroadcastService.shutdown();
        }

        if (groupsBroadcastService != null) {
            groupsBroadcastService.shutdown();
        }

        if (statsServicesBroadcastService != null) {
            statsServicesBroadcastService.shutdown();
        }

        if (serverBroadcastService != null) {
            serverBroadcastService.shutdown();
        }

        if (groupDetailsBroadcastService != null) {
            groupDetailsBroadcastService.shutdown();
        }

        if (playerBroadcastService != null) {
            playerBroadcastService.shutdown();
        }

        if (serviceDetailsBroadcastService != null) {
            serviceDetailsBroadcastService.shutdown();
        }

        if (sessionManager != null) {
            sessionManager.closeAll("WebInterface shutdown");
        }

        if (app != null) {
            app.stop();
        }

        cloudAPI.logger().info("WebInterface module stopped.");
    }
}
