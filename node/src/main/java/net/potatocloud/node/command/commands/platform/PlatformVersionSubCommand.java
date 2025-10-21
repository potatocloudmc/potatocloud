package net.potatocloud.node.command.commands.platform;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.setup.setups.AddVersionToPlatformSetup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@SubCommandInfo(
        name = "version",
        description = "Manage versions of the given platform",
        usage = "platform version &8[&aadd&8|&aremove&8|&alist&8|&ainfo&8] [&aplatform&8] [&aversion&8]"
)
public class PlatformVersionSubCommand extends SubCommand implements TabCompleter {

    private final PlatformManager platformManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            sendUsage();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> {
                if (args.length < 2) {
                    sendUsage();
                    return;
                }

                final Platform platform = platformManager.getPlatform(args[1]);
                if (platform == null) {
                    logger.info("This platform does &cnot &7exist");
                    return;
                }

                final Node node = Node.getInstance();

                node.getSetupManager().startSetup(new AddVersionToPlatformSetup(node.getConsole(), node.getScreenManager(), platform, node.getLogger()));
            }

            case "remove" -> {
                if (args.length < 3) {
                    sendUsage();
                    return;
                }

                final Platform platform = platformManager.getPlatform(args[1]);
                if (platform == null) {
                    logger.info("This platform does &cnot &7exist");
                    return;
                }

                final PlatformVersion version = platform.getVersion(args[2]);
                if (version == null) {
                    logger.info("This platform version does &cnot &7exist");
                    return;
                }

                final List<PlatformVersion> versions = new ArrayList<>(platform.getVersions());
                versions.remove(version);
                platform.setVersions(versions);
                platform.update();

                logger.info("Version &a" + version.getName() + " &7was removed from platform &a" + platform.getName());
            }

            case "list" -> {
                if (args.length < 2) {
                    sendUsage();
                    return;
                }

                final Platform platform = platformManager.getPlatform(args[1]);
                if (platform == null) {
                    logger.info("This platform does &cnot &7exist");
                    return;
                }

                final List<PlatformVersion> versions = platform.getVersions();
                if (versions.isEmpty()) {
                    logger.info("No versions found for platform &a" + platform.getName());
                    return;
                }

                logger.info("All versions for platform &a" + platform.getName() + "&8:");
                for (PlatformVersion version : versions) {
                    logger.info("&8» &a" + version.getName() + " &7- Legacy: " + (version.isLegacy() ? "&cYes" : "&aNo"));
                }
            }

            case "info" -> {
                if (args.length < 3) {
                    sendUsage();
                    return;
                }


                final Platform platform = platformManager.getPlatform(args[1]);
                if (platform == null) {
                    logger.info("This platform does &cnot &7exist");
                    return;
                }

                final PlatformVersion version = platform.getVersion(args[2]);
                if (version == null) {
                    logger.info("This platform version does &cnot &7exist");
                    return;
                }

                logger.info("Information for version &a" + version.getFullName() + "&8:");
                logger.info("&8» &7Platform: &a" + version.getPlatformName());
                logger.info("&8» &7Legacy: " + (version.isLegacy() ? "&cYes" : "&aNo"));
                logger.info("&8» &7Download URL: " + (version.getDownloadUrl() != null ? version.getDownloadUrl() : "&aAuto generated"));
            }

            default -> sendUsage();
        }
    }

    @Override
    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return Stream.of("add", "remove", "list", "info")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && List.of("add", "remove", "list", "info").contains(args[0].toLowerCase())) {
            return platformManager.getPlatforms().stream()
                    .map(Platform::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        if (args.length == 3 && List.of("add", "remove", "info").contains(args[0].toLowerCase())) {
            final Platform platform = platformManager.getPlatform(args[1]);
            if (platform != null) {
                return platform.getVersions().stream()
                        .map(PlatformVersion::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .toList();
            }
        }

        return List.of();
    }
}
