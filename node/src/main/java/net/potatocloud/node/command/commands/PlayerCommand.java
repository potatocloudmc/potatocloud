package net.potatocloud.node.command.commands;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.command.ArgumentType;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;

import java.util.Set;

@CommandInfo(name = "player", description = "Manage online players", aliases = {"players", "cloudplayer"})
public class PlayerCommand extends Command {

    public PlayerCommand(Logger logger, CloudPlayerManager playerManager) {
        defaultExecutor(_ -> sendHelp());

        sub("connect", "Connect a player to a service")
                .argument(ArgumentType.Player("player"))
                .argument(ArgumentType.Service("service"))
                .executes(ctx -> {
                    final CloudPlayer player = ctx.get("player");
                    final Service service = ctx.get("service");

                    final boolean alreadyConnected = player.service().map(s -> s.name().equals(service.name())).orElse(false);

                    if (alreadyConnected) {
                        logger.info("Player &a" + player.username() + " &7is already connected to &a" + service.name());
                        return;
                    }

                    playerManager.connectTo(player, service);
                    logger.info("Successfully connected player &a" + player.username() + " &7to service &a" + service.name());
                });

        sub("kick", "Kick a player from the network")
                .argument(ArgumentType.Player("player"))
                .optionalArgument(ArgumentType.MultiString("reason"))
                .executes(ctx -> {
                    final CloudPlayer player = ctx.get("player");
                    final String reason = ctx.get("reason");

                    if (reason == null) {
                        playerManager.kick(player);
                    } else {
                        playerManager.kick(player, reason);
                    }
                    logger.info("Kicked player &a" + player.username());
                });

        sub("list", "List online players")
                .executes(_ -> {
                    final Set<CloudPlayer> players = playerManager.players();
                    if (players.isEmpty()) {
                        logger.info("There are &cno &7online players");
                        return;
                    }
                    for (CloudPlayer player : players) {
                        logger.info("&8» &a" + player.username() + " &7- Proxy: &a" + player.proxy().name() + " &7- Service: &a" + player.service().map(Service::name).orElse("none"));
                    }
                });
    }
}
