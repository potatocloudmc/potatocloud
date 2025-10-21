package net.potatocloud.node.command.commands;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.command.commands.player.PlayerConnectSubCommand;
import net.potatocloud.node.command.commands.player.PlayerListSubCommand;
import net.potatocloud.node.console.Logger;

@RequiredArgsConstructor
@CommandInfo(name = "player", description = "Manage online players", aliases = {"players", "cloudplayer"})
public class PlayerCommand extends Command {

    public PlayerCommand(Logger logger, CloudPlayerManager playerManager, ServiceManager serviceManager) {
        addSubCommand(new PlayerConnectSubCommand(playerManager, logger, serviceManager));
        addSubCommand(new PlayerListSubCommand(playerManager, logger));
    }

    @Override
    public void execute(String[] args) {
        sendHelp();
    }
}
