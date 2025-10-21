package net.potatocloud.node.command.commands.platform;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "info", description = "Shows information of the given platform", usage = "platform info &8[&aplatform&8]")
public class PlatformInfoSubCommand extends SubCommand implements TabCompleter {

    private final PlatformManager platformManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            sendUsage();
            return;
        }

        final Platform platform = platformManager.getPlatform(args[0]);
        if (platform == null) {
            logger.info("This platform does &cnot &7exist");
            return;
        }

        logger.info("&7Info for platform &a" + platform.getName() + "&8:");

        if (platform.getDownloadUrl() != null) {
            logger.info("&8» &7Download URL: &a" + platform.getDownloadUrl());

        }
        logger.info("&8» &7Custom: " + (platform.isCustom() ? "&aYes" : "&cNo"));
        logger.info("&8» &7Proxy: " + (platform.isProxy() ? "&aYes" : "&cNo"));
        logger.info("&8» &7Base: &a" + platform.getBase());

        if (platform.getPreCacheBuilder() != null) {
            logger.info("&8» &7Pre-Cache Builder: &a" + platform.getPreCacheBuilder());
        }

        logger.info("&8» &7Versions: &a" + platform.getVersions().size());
        logger.info("&8» &7Prepare Steps: &a" + platform.getPrepareSteps().size());
        logger.info("&8» &7Parser: &a" + platform.getParser());
        logger.info("&8» &7Hash Type: &a" + platform.getHashType());

    }

    @Override
    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return platformManager.getPlatforms().stream()
                    .map(Platform::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
