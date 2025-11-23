package net.potatocloud.node.command.commands.group;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.command.TabCompleters;
import net.potatocloud.node.console.Logger;

import java.util.List;

@RequiredArgsConstructor
@SubCommandInfo(name = "info", description = "Show details of the given service group", usage = "group delete &8[&agroup&8]")
public class GroupInfoSubCommand extends SubCommand implements TabCompleter {

    private final ServiceGroupManager groupManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            sendUsage();
            return;
        }

        final String name = args[0];
        final ServiceGroup group = groupManager.getServiceGroup(name);
        if (group == null) {
            logger.info("&cNo service group found with the name &a" + name);
            return;
        }

        logger.info("&7Info for group &a" + group.getName() + "&8:");
        logger.info("&8» &7Platform: &a" + group.getPlatform().getName());
        logger.info("&8» &7Version: &a" + group.getPlatformVersion().getName());
        logger.info("&8» &7Templates: &a" + String.join(", ", group.getServiceTemplates()));
        logger.info("&8» &7Min Online Count: &a" + group.getMinOnlineCount());
        logger.info("&8» &7Max Online Count: &a" + group.getMaxOnlineCount());
        logger.info("&8» &7Online Players: &a" + CloudAPI.getInstance().getPlayerManager().getOnlinePlayersByGroup(group).size());
        logger.info("&8» &7Max Players: &a" + group.getMaxPlayers());
        logger.info("&8» &7Max Memory: &a" + group.getMaxMemory() + "MB");
        logger.info("&8» &7Fallback: " + (group.isFallback() ? "&aYes" : "&cNo"));
        logger.info("&8» &7Static: " + (group.isStatic() ? "&aYes" : "&cNo"));
    }

    @Override
    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return TabCompleters.group(args);
        }
        return List.of();
    }
}
