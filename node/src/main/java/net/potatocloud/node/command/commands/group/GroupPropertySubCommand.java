package net.potatocloud.node.command.commands.group;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.Property;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.command.SubCommandInfo;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@SubCommandInfo(
        name = "property",
        description = "Manage properties of the given service group",
        usage = "group property &8[&alist&8|&aset&8|&aremove&8] [&agroup&8] [&akey&8] [&avalue&8]"
)
public class GroupPropertySubCommand extends SubCommand implements TabCompleter {

    private final ServiceGroupManager groupManager;
    private final Logger logger;

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            sendUsage();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                if (args.length < 2) {
                    sendUsage();
                    return;
                }

                final String name = args[1];
                final ServiceGroup group = groupManager.getServiceGroup(name);
                if (group == null) {
                    logger.info("&cNo service group found with the name &a" + name);
                    return;
                }

                final List<Property<?>> properties = group.getProperties();

                if (properties.isEmpty()) {
                    logger.info("No properties found for group &a" + name);
                    return;
                }

                logger.info("Properties of group &a" + name + "&8:");
                for (Property<?> property : properties) {
                    logger.info("&8» &a" + property.getName() + " &7- " + property.getValue());
                }
            }
            case "remove" -> {
                if (args.length < 3) {
                    sendUsage();
                    return;
                }

                final String name = args[1];
                final ServiceGroup group = groupManager.getServiceGroup(name);
                if (group == null) {
                    logger.info("&cNo service group found with the name &a" + name);
                    return;
                }

                final String key = args[2].toLowerCase();
                final Property<?> property = group.getProperty(key);
                if (property == null) {
                    logger.info("Property &a" + key + "&7 was &cnot found &7in group &a" + name);
                    return;
                }

                group.getProperties().remove(property);
                group.update();
                logger.info("Property &a" + key + " &7was removed in group &a" + name);
            }
            case "set" -> {
                if (args.length < 4) {
                    sendUsage();
                    return;
                }

                final String name = args[1];
                final ServiceGroup group = groupManager.getServiceGroup(name);
                if (group == null) {
                    logger.info("&cNo service group found with the name &a" + name);
                    return;
                }

                final String key = args[2].toLowerCase();
                final String value = args[3];

                try {
                    group.setProperty(Property.of(key, value, value));
                    group.update();
                    logger.info("Property &a" + key + " &7was set to &a" + value + " &7in group &a" + name);
                } catch (Exception e) {
                    sendUsage();
                }
            }
            default -> sendUsage();
        }
    }

    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return Stream.of("list", "set", "remove")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            return groupManager.getAllServiceGroups().stream()
                    .map(ServiceGroup::getName)
                    .filter(name -> name.startsWith(args[1]))
                    .toList();
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("remove")) {
            final String groupName = args[1];
            if (groupManager.existsServiceGroup(groupName)) {
                return groupManager.getServiceGroup(groupName).getProperties().stream()
                        .map(Property::getName)
                        .filter(p -> p.startsWith(args[2]))
                        .toList();
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            final List<String> completions = new ArrayList<>();
            completions.add("<custom>");
            completions.addAll(DefaultProperties.asSet().stream()
                    .map(Property::getName)
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .toList());
            return completions;
        }

        return List.of();
    }
}
