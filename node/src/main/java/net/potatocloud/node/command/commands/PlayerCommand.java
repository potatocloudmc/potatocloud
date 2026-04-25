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
        defaultExecutor(ctx -> sendHelp());

        sub("connect", "Connect a player to a service")
                .argument(ArgumentType.Player("player"))
                .argument(ArgumentType.Service("service"))
                .executes(ctx -> {
                    final CloudPlayer player = ctx.get("player");
                    final Service service = ctx.get("service");

                    if (player.getConnectedServiceName().equalsIgnoreCase(service.getName())) {
                        logger.info("Player &a" + player.getUsername() + " &7is already connected to &a" + service.getName());
                        return;
                    }

                    player.connectWithService(service);
                    logger.info("Successfully connected player &a" + player.getUsername() + " &7to service &a" + service.getName());
                });

        sub("list", "List online players")
                .executes(ctx -> {
                    final Set<CloudPlayer> players = playerManager.getOnlinePlayers();
                    if (players.isEmpty()) {
                        logger.info("There are &cno &7online players");
                        return;
                    }
                    for (CloudPlayer player : players) {
                        logger.info("&8» &a" + player.getUsername() + " &7- Proxy: &a" + player.getConnectedProxyName() + " &7- Service: &a" + player.getConnectedServiceName());
                    }
                });
    }
}
