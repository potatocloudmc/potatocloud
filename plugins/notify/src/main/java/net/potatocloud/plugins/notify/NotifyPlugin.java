package net.potatocloud.plugins.notify;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.event.events.service.PreparedServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStartedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.api.service.Service;

import java.util.logging.Logger;

public class NotifyPlugin {

    private final ProxyServer server;
    private final CloudAPI cloudAPI = CloudAPI.getInstance();
    private final Logger logger;
    private final MessagesConfig messages;
    private final Config config;

    @Inject
    public NotifyPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        messages = new MessagesConfig();
        config = new Config();

        messages.load();
        config.load();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        final EventManager eventManager = cloudAPI.getEventManager();

        if (config.enableStarting()) {
            eventManager.on(PreparedServiceStartingEvent.class, startingEvent -> sendMessage(startingEvent.getServiceName(), "service-starting", false));
        }

        eventManager.on(ServiceStartedEvent.class, startedEvent -> sendMessage(startedEvent.getServiceName(), "service-started", true));

        if (config.enableStopping()) {
            eventManager.on(ServiceStoppingEvent.class, stoppingEvent -> sendSimpleMessage("service-stopping", stoppingEvent.getServiceName()));
        }

        eventManager.on(ServiceStoppedEvent.class, stoppedEvent -> sendSimpleMessage("service-stopped", stoppedEvent.getServiceName()));
    }

    private void sendMessage(String serviceName, String key, boolean clickEvent) {
        final Service service = cloudAPI.getServiceManager().getService(serviceName);

        Component message = messages.get(key)
                .replaceText(text -> text.match("%service%").replacement(service.getName()))
                .replaceText(text -> text.match("%port%").replacement(String.valueOf(service.getPort())))
                .replaceText(text -> text.match("%group%").replacement(service.getServiceGroup().getName()));

        if (clickEvent) {
            message = message.clickEvent(ClickEvent.runCommand("/server " + serviceName)).hoverEvent(HoverEvent.showText(
                    messages.get("hover-text").replaceText(text -> text.match("%service%").replacement(service.getName()))
            ));
        }

        final Component finalMessage = message;
        server.getAllPlayers().stream()
                .filter(p -> p.hasPermission(config.getPermission()))
                .forEach(p -> p.sendMessage(finalMessage));
    }

    private void sendSimpleMessage(String key, String serviceName) {
        server.getAllPlayers().stream()
                .filter(player -> player.hasPermission(config.getPermission()))
                .forEach(player -> player.sendMessage(messages.get(key).replaceText(text -> text.match("%service%").replacement(serviceName))));
    }
}
