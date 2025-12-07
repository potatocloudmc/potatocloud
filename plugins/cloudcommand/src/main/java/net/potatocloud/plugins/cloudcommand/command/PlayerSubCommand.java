package net.potatocloud.plugins.cloudcommand.command;

import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.service.Service;
import net.potatocloud.plugins.cloudcommand.MessagesConfig;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class PlayerSubCommand {

    private final Player player;
    private final MessagesConfig messages;
    private final CloudPlayerManager playerManager = CloudAPI.getInstance().getPlayerManager();

    public void sendHelp() {
        player.sendMessage(messages.get("player.help.list"));
        player.sendMessage(messages.get("player.help.connect"));
    }

    public void listPlayers() {
        final Set<CloudPlayer> players = playerManager.getOnlinePlayers();
        player.sendMessage(messages.get("player.list.header"));
        for (CloudPlayer cloudPlayer : players) {
            player.sendMessage(messages.get("player.list.entry")
                    .replaceText(text -> text.match("%name%").replacement(cloudPlayer.getUsername()))
                    .replaceText(text -> text.match("%service%").replacement(cloudPlayer.getConnectedServiceName()))
                    .replaceText(text -> text.match("%proxy%").replacement(cloudPlayer.getConnectedProxyName())));
        }
    }

    public void connectPlayer(String[] args) {
        if (args.length < 4) {
            player.sendMessage(messages.get("player.connect.usage"));
            return;
        }

        final String playerName = args[2];
        final String serviceName = args[3];

        final CloudPlayer cloudPlayer = playerManager.getCloudPlayer(playerName);
        if (playerName == null) {
            player.sendMessage(messages.get("no-player"));
            return;
        }

        final Service service = CloudAPI.getInstance().getServiceManager().getService(serviceName);
        if (service == null) {
            player.sendMessage(messages.get("no-service"));
            return;
        }

        if (cloudPlayer.getConnectedServiceName().equals(service.getName())) {
            player.sendMessage(messages.get("player.connect.already-connected")
                    .replaceText(text -> text.match("%player%").replacement(cloudPlayer.getUsername()))
                    .replaceText(text -> text.match("%service%").replacement(service.getName())));
            return;
        }

        cloudPlayer.connectWithService(service);
        player.sendMessage(messages.get("player.connect.success")
                .replaceText(text -> text.match("%player%").replacement(cloudPlayer.getUsername()))
                .replaceText(text -> text.match("%service%").replacement(service.getName())));
    }

    public List<String> suggest(String[] args) {
        if (args.length == 2) {
            return List.of("list", "connect").stream()
                    .filter(input -> input.startsWith(args[1].toLowerCase()))
                    .toList();
        }

        final String sub = args[1].toLowerCase();

        if (sub.equalsIgnoreCase("connect")) {
            if (args.length == 3) {
                return playerManager.getOnlinePlayers().stream().map(CloudPlayer::getUsername).
                        filter(input -> input.startsWith(args[2])).toList();
            }
            if (args.length == 4) {
                return CloudAPI.getInstance().getServiceManager().getAllServices().stream()
                        .filter(service -> !service.getServiceGroup().getPlatform().isProxy())
                        .map(Service::getName)
                        .filter(name -> name.startsWith(args[3]))
                        .toList();
            }
        }
        return List.of();
    }
}
