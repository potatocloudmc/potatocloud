package net.potatocloud.node.command.commands;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.command.commands.platform.PlatformDownloadSubCommand;
import net.potatocloud.node.command.commands.platform.PlatformListSubCommand;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.platform.DownloadManager;

@RequiredArgsConstructor
@CommandInfo(name = "platform", description = "Manage your platforms", aliases = {"platforms"})
public class PlatformCommand extends Command {

    public PlatformCommand(Logger logger, PlatformManager platformManager, DownloadManager downloadManager) {
        addSubCommand(new PlatformDownloadSubCommand(platformManager, logger, downloadManager));
        addSubCommand(new PlatformListSubCommand(logger, platformManager));
    }

    @Override
    public void execute(String[] args) {
        sendHelp();
    }
}
