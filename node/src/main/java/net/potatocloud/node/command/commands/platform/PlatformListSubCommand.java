package net.potatocloud.node.command.commands.platform;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.console.Logger;

@RequiredArgsConstructor
@SubCommandInfo(name = "list", description = "List all available platforms", usage = "platform list")
public class PlatformListSubCommand extends SubCommand {

    private final PlatformManager platformManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        logger.info("&7Available platforms&8:");
        for (Platform platform : platformManager.getPlatforms()) {
            logger.info("&8» &a" + platform.getName() +
                    " &7- Proxy: &a" + platform.isProxy() +
                    " &7- Custom: &a" + platform.isCustom());
        }
    }
}
