package net.potatocloud.node.command.commands.platform;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.platform.DownloadManager;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "download", description = "Downloads the given version", usage = "platform download &8[&aplatform&8] &8[&aversion&8]")
public class PlatformDownloadSubCommand extends SubCommand implements TabCompleter {

    private final PlatformManager platformManager;
    private final Logger logger;
    private final DownloadManager downloadManager;

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            sendUsage();
            return;
        }

        final Platform platform = platformManager.getPlatform(args[0]);
        if (platform == null) {
            logger.info("This platform does &cnot &7exist");
            return;
        }

        final PlatformVersion version = platform.getVersion(args[1]);
        if (version == null) {
            logger.info("This version does &7not &7exist for the given platform");
            return;
        }

        downloadManager.downloadPlatformVersion(platform, version);
    }

    @Override
    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return platformManager.getPlatforms().stream()
                    .map(Platform::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            final Platform platform = platformManager.getPlatform(args[0]);
            if (platform == null) {
                return List.of();
            }
            return platform.getVersions().stream()
                    .map(PlatformVersion::getName)
                    .filter(ver -> ver.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
