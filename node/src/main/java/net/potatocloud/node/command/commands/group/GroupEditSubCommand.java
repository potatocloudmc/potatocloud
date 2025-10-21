package net.potatocloud.node.command.commands.group;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@SubCommandInfo(
        name = "edit",
        description = "Edit the given service group",
        usage = "group edit &8[&agroup&8] [&akey&8|&aaddTemplate&8|&aremoveTemplate&8|&aaddJvmFlag&8] [&avalue&8]"
)
public class GroupEditSubCommand extends SubCommand implements TabCompleter {

    private final ServiceGroupManager groupManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            sendUsage();
            return;
        }

        final String name = args[0];
        final ServiceGroup group = groupManager.getServiceGroup(name);
        if (group == null) {
            logger.info("&cNo service group found with the name &a" + name);
            return;
        }

        final String key = args[1].toLowerCase();
        final String value = args[2];

        try {
            switch (key) {
                case "addtemplate" -> {
                    group.addServiceTemplate(value);
                    Node.getInstance().getTemplateManager().createTemplate(value);
                    group.update();
                    logger.info("Template &a" + value + " &7was added to group &a" + name);
                    return;
                }
                case "removetemplate" -> {
                    if (group.getServiceTemplates().removeIf(s -> s.equalsIgnoreCase(value))) {
                        group.update();
                        logger.info("Template &a" + value + " &7was removed from group &a" + name);
                    } else {
                        logger.info("Template &a" + value + " &7was not found in group &a" + name);
                    }
                    return;
                }
                case "addjvmflag" -> {
                    group.addCustomJvmFlag(value);
                    group.update();
                    logger.info("Custom JVM flag &a" + value + " &7was added to group &a" + name);
                    return;
                }

                case "minonlinecount" -> group.setMinOnlineCount(Integer.parseInt(value));
                case "maxonlinecount" -> group.setMaxOnlineCount(Integer.parseInt(value));
                case "maxplayers" -> group.setMaxPlayers(Integer.parseInt(value));
                case "maxmemory" -> group.setMaxMemory(Integer.parseInt(value));
                case "fallback" -> group.setFallback(Boolean.parseBoolean(value));
                case "startpercentage" -> group.setStartPercentage(Integer.parseInt(value));
                case "startpriority" -> group.setStartPriority(Integer.parseInt(value));

                default -> {
                    sendUsage();
                    return;
                }
            }
            group.update();
            logger.info("Updated &a" + key + " &7for group &a" + name + "&7 to &a" + value);
        } catch (NumberFormatException ex) {
            sendUsage();
        }
    }

    @Override
    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return groupManager.getAllServiceGroups().stream()
                    .map(ServiceGroup::getName)
                    .filter(name -> name.startsWith(args[0]))
                    .toList();
        }

        if (args.length == 2) {
            return Stream.of("minOnlineCount", "maxOnlineCount", "maxPlayers", "maxMemory", "fallback",
                            "startPercentage", "startPriority", "addTemplate", "removeTemplate", "addJvmFlag")
                    .filter(key -> key.startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}
