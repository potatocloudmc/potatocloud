package net.potatocloud.node.command.commands.player;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.console.Logger;

import java.util.Set;

@RequiredArgsConstructor
@SubCommandInfo(name = "list", description = "List online players of the network", usage = "player list")
public class PlayerListSubCommand extends SubCommand {

    private final CloudPlayerManager playerManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        final Set<CloudPlayer> players = playerManager.getOnlinePlayers();
        if (players.isEmpty()) {
            logger.info("There are &cno &7online players");
            return;
        }
        for (CloudPlayer player : players) {
            logger.info("&8» &a" + player.getUsername() + " &7- Proxy: &a" + player.getConnectedProxyName() + " &7- Service: &a" + player.getConnectedServiceName());
        }
    }
}
