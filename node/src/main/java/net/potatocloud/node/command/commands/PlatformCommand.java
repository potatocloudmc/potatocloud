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
import java.util.Optional;

@CommandInfo(name = "platform", description = "Manage platforms", aliases = {"platforms"})
public class PlatformCommand extends Command {

    public PlatformCommand(Logger logger, PlatformManager platformManager) {
        final Node node = Node.instance();

        defaultExecutor(ctx -> sendHelp());

        sub("create", "Create a new platform").executes(ctx -> {
            node.setupManager().startSetup(new PlatformConfigurationSetup(node.console(), node.screenManager(), node.platformManager(), node.logger()));
        });

        sub("download", "Download a platform version")
                .argument(ArgumentType.Platform("platform"))
                .argument(ArgumentType.String("version"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("platform") || argsLength != 1) {
                        return List.of();
                    }

                    final Platform platform = ctx.get("platform");

                    return platform.versions()
                            .stream()
                            .map(PlatformVersion::name)
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");
                    final String versionName = ctx.get("version");
                    final Optional<PlatformVersion> version = platform.version(versionName);

                    if (version.isEmpty()) {
                        logger.info("&cNo version found with the name &a" + versionName + " in platform &a" + platform.name());
                        return;
                    }

                    node.downloadManager().downloadPlatformVersion(platform, version.get());
                });

        sub("info", "Shows information of a platform")
                .argument(ArgumentType.Platform("platform"))
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");

                    logger.info("&7Info for platform &a" + platform.name() + "&8:");

                    if (platform.downloadUrl() != null) {
                        logger.info("&8» &7Download URL: &a" + platform.downloadUrl());

                    }
                    logger.info("&8» &7Custom: " + (platform.custom() ? "&aYes" : "&cNo"));
                    logger.info("&8» &7Proxy: " + (platform.proxy() ? "&aYes" : "&cNo"));
                    logger.info("&8» &7Base: &a" + platform.base().id());

                    if (platform.preCacheBuilder() != null) {
                        logger.info("&8» &7Pre-Cache Builder: &a" + platform.preCacheBuilder());
                    }

                    logger.info("&8» &7Versions: &a" + platform.versions().size());
                    logger.info("&8» &7Prepare Steps: &a" + platform.prepareSteps().size());
                    logger.info("&8» &7Parser: &a" + platform.parser());
                    logger.info("&8» &7Hash Type: &a" + platform.hashType());
                });

        sub("list", "List all platforms")
                .executes(ctx -> {
                    logger.info("&7Available platforms:");

                    for (Platform platform : platformManager.platforms()) {
                        logger.info("&8» &a" + platform.name() +
                                " &7- Proxy: &a" + platform.proxy() +
                                " &7- Custom: &a" + platform.custom());
                    }
                });

        final SubCommand versionSub = sub("version", "Manage versions of a platform");

        versionSub.executes(ctx -> versionSub.sendHelp());

        versionSub.sub("add")
                .argument(ArgumentType.Platform("platform"))
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");

                    node.setupManager().startSetup(new AddVersionToPlatformSetup(node.console(), node.screenManager(), platform, logger));
                });

        versionSub.sub("remove")
                .argument(ArgumentType.Platform("platform"))
                .argument(ArgumentType.String("version"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("platform") || argsLength != 1) {
                        return List.of();
                    }

                    final Platform platform = ctx.get("platform");

                    return platform.versions()
                            .stream()
                            .map(PlatformVersion::name)
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");
                    final String versionName = ctx.get("version");
                    final Optional<PlatformVersion> version = platform.version(versionName);

                    if (version.isEmpty()) {
                        logger.info("&cNo version found with the name &a" + versionName + " in platform &a" + platform.name());
                        return;
                    }

                    final List<PlatformVersion> versions = new ArrayList<>(platform.versions());
                    versions.remove(version);
                    platform.versions(versions);
                    platformManager.update(platform);

                    logger.info("Version &a" + version.get().name() + " &7was removed from platform &a" + platform.name());
                });

        versionSub.sub("list")
                .argument(ArgumentType.Platform("platform"))
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");

                    final List<PlatformVersion> versions = platform.versions();
                    if (versions.isEmpty()) {
                        logger.info("No versions found for platform &a" + platform.name());
                        return;
                    }

                    logger.info("All versions for platform &a" + platform.name() + "&8:");
                    for (PlatformVersion version : versions) {
                        logger.info("&8» &a" + version.name() + " &7- Legacy: " + (version.legacy() ? "&cYes" : "&aNo"));
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

                    logger.info(String.valueOf(platform.versions().size()));

                    return platform.versions()
                            .stream()
                            .map(PlatformVersion::name)
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Platform platform = ctx.get("platform");
                    final String versionName = ctx.get("version");
                    final Optional<PlatformVersion> version = platform.version(versionName);

                    if (version.isEmpty()) {
                        logger.info("&cNo version found with the name &a" + versionName + " in platform &a" + platform.name());
                        return;
                    }

                    logger.info("Information for version &a" + version.get().fullName() + "&8:");
                    logger.info("&8» &7Platform: &a" + version.get().platform().name());
                    logger.info("&8» &7Legacy: " + (version.get().legacy() ? "&cYes" : "&aNo"));
                    logger.info("&8» &7Download URL: " + (version.get().downloadUrl() != null ? version.get().downloadUrl() : "&aAuto generated"));
                });
    }
}
