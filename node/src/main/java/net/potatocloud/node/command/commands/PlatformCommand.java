package net.potatocloud.node.command.commands;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.ArgumentType;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.setup.setups.AddVersionToPlatformSetup;
import net.potatocloud.node.setup.setups.PlatformConfigurationSetup;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(name = "platform", description = "Manage platforms", aliases = {"platforms"})
public class PlatformCommand extends Command {

    public PlatformCommand(Logger logger, PlatformManager platformManager) {
        final Node node = Node.getInstance();

        defaultExecutor(ctx -> sendHelp());

        sub("create", "Create a new platform").executes(ctx -> {
            node.getSetupManager().startSetup(new PlatformConfigurationSetup(node.getConsole(), node.getScreenManager(), node.getPlatformManager(), node.getLogger()));
        });

        sub("download", "Download a platform version")
                .argument(ArgumentType.Platform("platform"))
                .argument(ArgumentType.String("version"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("platform") || argsLength != 1) {
                        return List.of();
                    }

                    final Platform platform = ctx.get("platform");

                    return platform.getVersions()
                            .stream()
                            .map(PlatformVersion::getName)
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");
                    final String versionName = ctx.get("version");
                    final PlatformVersion version = platform.getVersion(versionName);

                    if (version == null) {
                        logger.info("&cNo version found with the name &a" + versionName + " in platform &a" + platform.getName());
                        return;
                    }

                    node.getDownloadManager().downloadPlatformVersion(platform, version);
                });

        sub("info", "Shows information of a platform")
                .argument(ArgumentType.Platform("platform"))
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");

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
                });

        sub("list", "List all platforms")
                .executes(ctx -> {
                    logger.info("&7Available platforms:");

                    for (Platform platform : platformManager.getPlatforms()) {
                        logger.info("&8» &a" + platform.getName() +
                                " &7- Proxy: &a" + platform.isProxy() +
                                " &7- Custom: &a" + platform.isCustom());
                    }
                });

        final SubCommand versionSub = sub("version", "Manage versions of a platform");

        versionSub.executes(ctx -> versionSub.sendHelp());

        versionSub.sub("add")
                .argument(ArgumentType.Platform("platform"))
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");

                    node.getSetupManager().startSetup(new AddVersionToPlatformSetup(node.getConsole(), node.getScreenManager(), platform, logger));
                });

        versionSub.sub("remove")
                .argument(ArgumentType.Platform("platform"))
                .argument(ArgumentType.String("version"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("platform") || argsLength != 1) {
                        return List.of();
                    }

                    final Platform platform = ctx.get("platform");

                    return platform.getVersions()
                            .stream()
                            .map(PlatformVersion::getName)
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");
                    final String versionName = ctx.get("version");
                    final PlatformVersion version = platform.getVersion(versionName);

                    if (version == null) {
                        logger.info("&cNo version found with the name &a" + versionName + " in platform &a" + platform.getName());
                        return;
                    }

                    final List<PlatformVersion> versions = new ArrayList<>(platform.getVersions());
                    versions.remove(version);
                    platform.setVersions(versions);
                    platform.update();

                    logger.info("Version &a" + version.getName() + " &7was removed from platform &a" + platform.getName());
                });

        versionSub.sub("list")
                .argument(ArgumentType.Platform("platform"))
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");

                    final List<PlatformVersion> versions = platform.getVersions();
                    if (versions.isEmpty()) {
                        logger.info("No versions found for platform &a" + platform.getName());
                        return;
                    }

                    logger.info("All versions for platform &a" + platform.getName() + "&8:");
                    for (PlatformVersion version : versions) {
                        logger.info("&8» &a" + version.getName() + " &7- Legacy: " + (version.isLegacy() ? "&cYes" : "&aNo"));
                    }
                });

        versionSub.sub("info")
                .argument(ArgumentType.Platform("platform"))
                .argument(ArgumentType.String("version"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("platform") || argsLength != 1) {
                        return List.of();
                    }

                    final Platform platform = ctx.get("platform");

                    logger.info(String.valueOf(platform.getVersions().size()));

                    return platform.getVersions()
                            .stream()
                            .map(PlatformVersion::getName)
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");
                    final String versionName = ctx.get("version");
                    final PlatformVersion version = platform.getVersion(versionName);

                    if (version == null) {
                        logger.info("&cNo version found with the name &a" + versionName + " in platform &a" + platform.getName());
                        return;
                    }

                    logger.info("Information for version &a" + version.getFullName() + "&8:");
                    logger.info("&8» &7Platform: &a" + version.getPlatformName());
                    logger.info("&8» &7Legacy: " + (version.isLegacy() ? "&cYes" : "&aNo"));
                    logger.info("&8» &7Download URL: " + (version.getDownloadUrl() != null ? version.getDownloadUrl() : "&aAuto generated"));
                });
    }
}
