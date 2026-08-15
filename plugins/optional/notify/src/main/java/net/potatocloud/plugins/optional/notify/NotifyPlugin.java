package net.potatocloud.plugins.optional.notify;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.service.ServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStartedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.common.config.Config;
import net.potatocloud.common.config.yaml.YamlConfig;
import net.potatocloud.plugins.shared.MessagesConfig;

import java.nio.file.Path;

public class NotifyPlugin {

    private final ProxyServer server;
    private final CloudAPI cloudAPI = CloudAPI.instance();
    private final Config config;
    private final MessagesConfig messages;

    @Inject
    public NotifyPlugin(ProxyServer server) {
        this.server = server;
        final String folder = "plugins/potatocloud-notify";

        config = new YamlConfig(Path.of(folder).resolve("config.yml"));
        messages = new MessagesConfig(folder);
        config.load();
        messages.load();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        final EventBus eventBus = cloudAPI.eventBus();

        if (config.get("messages.enable-service-starting").asBoolean()) {
            eventBus.subscribe(ServiceStartingEvent.class, startingEvent -> sendMessage(startingEvent.serviceName(), "service-starting", false));
        }

        eventBus.subscribe(ServiceStartedEvent.class, startedEvent -> sendMessage(startedEvent.serviceName(), "service-started", true));

        if (config.get("messages.enable-service-stopping").asBoolean()) {
            eventBus.subscribe(ServiceStoppingEvent.class, stoppingEvent -> sendSimpleMessage("service-stopping", stoppingEvent.serviceName()));
        }

        eventBus.subscribe(ServiceStoppedEvent.class, stoppedEvent -> sendSimpleMessage("service-stopped", stoppedEvent.serviceName()));
    }

    private void sendMessage(String serviceName, String key, boolean clickEvent) {
        cloudAPI.serviceManager().find(serviceName).ifPresent(service -> {
            Component message = messages.get(key)
                    .replaceText(text -> text.match("%service%").replacement(service.name()))
                    .replaceText(text -> text.match("%port%").replacement(String.valueOf(service.port())))
                    .replaceText(text -> text.match("%group%").replacement(service.group().name()));

            if (clickEvent) {
                message = message.clickEvent(ClickEvent.runCommand("/server " + serviceName)).hoverEvent(HoverEvent.showText(
                        messages.get("hover-text").replaceText(text -> text.match("%service%").replacement(service.name()))
                ));
            }

            final Component finalMessage = message;
            server.getAllPlayers().stream()
                    .filter(player -> player.hasPermission(config.get("permission").asString()))
                    .forEach(player -> player.sendMessage(finalMessage));
        });
    }

    private void sendSimpleMessage(String key, String serviceName) {
        server.getAllPlayers().stream()
                .filter(player -> player.hasPermission(config.get("permission").asString()))
                .forEach(player -> player.sendMessage(messages.get(key).replaceText(text -> text.match("%service%").replacement(serviceName))));
    }
}
