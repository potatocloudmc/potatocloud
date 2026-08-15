package net.potatocloud.plugins.optional.cloudcommand.command;

import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.service.Service;
import net.potatocloud.plugins.shared.MessagesConfig;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class PlayerSubCommand {

    private final Player player;
    private final MessagesConfig messages;
    private final CloudPlayerManager playerManager = CloudAPI.instance().playerManager();

    public void sendHelp() {
        player.sendMessage(messages.get("player.help.list"));
        player.sendMessage(messages.get("player.help.connect"));
    }

    public void listPlayers() {
        final Set<CloudPlayer> players = playerManager.players();
        player.sendMessage(messages.get("player.list.header"));
        for (CloudPlayer cloudPlayer : players) {
            player.sendMessage(messages.get("player.list.entry")
                    .replaceText(text -> text.match("%name%").replacement(cloudPlayer.username()))
                    .replaceText(text -> text.match("%service%").replacement(cloudPlayer.service().map(Service::name).orElse("none")))
                    .replaceText(text -> text.match("%proxy%").replacement(cloudPlayer.proxy().name())));
        }
    }

    public void connectPlayer(String[] args) {
        if (args.length < 4) {
            player.sendMessage(messages.get("player.connect.usage"));
            return;
        }

        final String playerName = args[2];
        final String serviceName = args[3];

        playerManager.find(playerName).ifPresentOrElse(cloudPlayer -> {
            if (playerName == null) {
                player.sendMessage(messages.get("no-player"));
                return;
            }

            CloudAPI.instance().serviceManager().find(serviceName).ifPresent(service -> {
                final boolean alreadyConnected = cloudPlayer.service().map(s -> s.name().equals(service.name())).orElse(false);

                if (alreadyConnected) {
                    player.sendMessage(messages.get("player.connect.already-connected")
                            .replaceText(text -> text.match("%player%").replacement(cloudPlayer.username()))
                            .replaceText(text -> text.match("%service%").replacement(service.name())));
                    return;
                }

                CloudAPI.instance().playerManager().connectTo(cloudPlayer, service);
                player.sendMessage(messages.get("player.connect.success")
                        .replaceText(text -> text.match("%player%").replacement(cloudPlayer.username()))
                        .replaceText(text -> text.match("%service%").replacement(service.name())));
            });
        }, () -> player.sendMessage(messages.get("no-player")));
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
                return playerManager.players().stream().map(CloudPlayer::username).
                        filter(input -> input.startsWith(args[2])).toList();
            }
            if (args.length == 4) {
                return CloudAPI.instance().serviceManager().services().stream()
                        .filter(service -> !service.group().platform().proxy())
                        .map(Service::name)
                        .filter(name -> name.startsWith(args[3]))
                        .toList();
            }
        }
        return List.of();
    }
}
