package net.potatocloud.plugin.server.hub.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.plugin.server.shared.MessagesConfig;

import java.util.Comparator;
import java.util.Optional;

@RequiredArgsConstructor
public class HubCommand implements SimpleCommand {

    private final MessagesConfig messagesConfig;
    private final ProxyServer server;

    @Override
    public void execute(Invocation invocation) {
        final CommandSource source = invocation.source();

        if (!(source instanceof Player player)) {
            return;
        }

        final Service playerService = CloudAPI.getInstance().getPlayerManager().getCloudPlayer(player.getUniqueId()).getConnectedService();
        if (playerService.getServiceGroup().isPrimary()) {
            player.sendMessage(messagesConfig.get("alreadyOnPrimary"));
            return;
        }

        final Optional<RegisteredServer> primary = getBestPrimaryServer();
        if (primary.isEmpty()) {
            player.sendMessage(messagesConfig.get("noPrimaryFound"));
            return;
        }

        final RegisteredServer registeredServer = primary.get();
        player.createConnectionRequest(registeredServer).fireAndForget();
        player.sendMessage(messagesConfig.get("connect")
                .replaceText(text -> text.match("%service%").replacement(registeredServer.getServerInfo().getName())));
    }

    private Optional<RegisteredServer> getBestPrimaryServer() {
        return CloudAPI.getInstance().getServiceManager().getAllServices().stream()
                .filter(service -> service.getServiceGroup().isPrimary())
                .filter(service -> service.getStatus() == ServiceStatus.RUNNING)
                .sorted(Comparator.comparingInt(Service::getOnlinePlayerCount))
                .map(service -> server.getServer(service.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
