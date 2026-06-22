package net.potatocloud.node.command.commands;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.ArgumentType;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.command.SubCommand;
import net.potatocloud.node.setup.setups.GroupConfigurationSetup;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(name = "group", description = "Manage groups", aliases = {"groups", "g"})
public class GroupCommand extends Command {

    public GroupCommand(Logger logger, GroupManager groupManager) {
        final Node node = Node.instance();

        defaultExecutor(_ -> sendHelp());

        sub("create", "Create a new group")
                .executes(_ -> node.setupManager().startSetup(new GroupConfigurationSetup(
                        node.console(),
                        node.screenManager(),
                        groupManager,
                        node.platformManager())
                ));

        sub("delete", "Delete a group")
                .argument(ArgumentType.Group("group"))
                .executes(ctx -> {
                    final Group group = ctx.get("group");

                    groupManager.delete(group);
                    logger.info("&7Group &a" + group.name() + " &7was deleted");
                });

        sub("list", "List all groups")
                .executes(_ -> {
                    final List<Group> groups = groupManager.groups();

                    if (groups.isEmpty()) {
                        logger.info("There are &cno &7groups");
                        return;
                    }

                    logger.info("Loaded groups&8:");
                    for (Group group : groups) {
                        logger.info("&8» &a" + group.name());
                    }
                });

        sub("info", "Show details of a group")
                .argument(ArgumentType.Group("group"))
                .executes(ctx -> {
                    final Group group = ctx.get("group");

                    logger.info("&7Info for group &a" + group.name() + "&8:");
                    if (node.config().cluster().enabled() && group.node().isPresent()) {
                        logger.info("&8» &7Node: &a" + group.node().map(ClusterNode::name).orElse("Unknown"));
                    }
                    logger.info("&8» &7Platform: &a" + group.platform().name());
                    logger.info("&8» &7Version: &a" + group.platformVersion().name());
                    logger.info("&8» &7Templates: &a" + String.join(", ", group.templates()));
                    logger.info("&8» &7Min Online Count: &a" + group.minServices());
                    logger.info("&8» &7Max Online Count: &a" + group.maxServices());
                    logger.info("&8» &7Online Players: &a" + group.players().size());
                    logger.info("&8» &7Max Players: &a" + group.maxPlayers());
                    logger.info("&8» &7Max Memory: &a" + group.maxMemory() + "MB");
                    logger.info("&8» &7Fallback: " + (group.fallback() ? "&aYes" : "&cNo"));
                    logger.info("&8» &7Static: " + (group.staticServices() ? "&aYes" : "&cNo"));
                });

        sub("stop", "Stop all services in a group")
                .argument(ArgumentType.Group("group"))
                .executes(ctx -> {
                    final Group group = ctx.get("group");

                    for (Service service : group.services()) {
                        CloudAPI.instance().serviceManager().stop(service); // todo
                    }
                });

        final SubCommand propertySub = sub("property", "Manage properties of a group");

        propertySub.executes(_ -> propertySub.sendHelp());

        propertySub.sub("set")
                .argument(ArgumentType.Group("group"))
                .argument(ArgumentType.String("key"))
                .argument(ArgumentType.String("value"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("group") || argsLength != 1) {
                        return List.of();
                    }

                    final List<String> suggestions = new ArrayList<>();

                    for (Property<?> property : DefaultProperties.asSet()) {
                        suggestions.add(property.name());
                    }

                    suggestions.add("<custom>");

                    return suggestions.stream()
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Group group = ctx.get("group");
                    final String key = ctx.get("key");
                    final String value = ctx.get("value");

                    try {
                        final Property<?> property = PropertyUtil.stringToProperty(key, value);

                        group.set(property);
                        groupManager.update(group);
                        logger.info("Property &a" + key + " &7was set to &a" + value + " &7in group &a" + group.name());
                    } catch (Exception e) {
                        propertySub.sendHelp();
                    }
                });

        propertySub.sub("remove")
                .argument(ArgumentType.Group("group"))
                .argument(ArgumentType.String("key"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("group") || argsLength != 1) {
                        return List.of();
                    }

                    final Group group = ctx.get("group");

                    return group.properties().stream()
                            .map(Property::name)
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Group group = ctx.get("group");
                    final String key = ctx.get("key");

                    final Property<?> property = group.property(key);
                    if (property == null) {
                        logger.info("Property &a" + key + "&7 was &cnot found &7in group &a" + group.name());
                        return;
                    }

                    group.propertyMap().remove(property.name());
                    groupManager.update(group);
                    logger.info("Property &a" + key + " &7was removed in group &a" + group.name());
                });

        propertySub.sub("list")
                .argument(ArgumentType.Group("group"))
                .executes(ctx -> {
                    final Group group = ctx.get("group");
                    final List<Property<?>> properties = group.properties();

                    if (properties.isEmpty()) {
                        logger.info("No properties found for group &a" + group.name());
                        return;
                    }

                    logger.info("Properties of group &a" + group.name() + "&8:");
                    for (Property<?> property : properties) {
                        logger.info("&8» &a" + property.name() + " &7- " + property.value());
                    }
                });

        sub("edit", "Edit a group")
                .argument(ArgumentType.Group("group"))
                .argument(ArgumentType.String("key"))
                .argument(ArgumentType.String("value"))
                .suggests((ctx, input, argsLength) -> {
                    if (!ctx.has("group") || argsLength != 1) {
                        return List.of();
                    }

                    final List<String> suggestions = List.of(
                            "addTemplate",
                            "removeTemplate",
                            "addJvmFlag",
                            "minOnlineCount",
                            "maxOnlineCount",
                            "maxPlayers",
                            "maxMemory",
                            "fallback",
                            "startPercentage",
                            "startPriority"
                    );

                    return suggestions.stream()
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final Group group = ctx.get("group");
                    String key = ctx.get("key");
                    final String value = ctx.get("value");

                    key = key.toLowerCase();

                    final String groupName = group.name();

                    try {
                        switch (key) {
                            case "addtemplate" -> {
                                group.addTemplate(value);
                                Node.instance().templateManager().createTemplate(value);
                                groupManager.update(group);
                                logger.info("Template &a" + value + " &7was added to group &a" + groupName);
                                return;
                            }
                            case "removetemplate" -> {
                                if (group.templates().removeIf(s -> s.equalsIgnoreCase(value))) {
                                    groupManager.update(group);
                                    logger.info("Template &a" + value + " &7was removed from group &a" + groupName);
                                } else {
                                    logger.info("Template &a" + value + " &7was not found in group &a" + groupName);
                                }
                                return;
                            }
                            case "addjvmflag" -> {
                                group.addCustomJvmFlag(value);
                                groupManager.update(group);
                                logger.info("Added JVM flag &a" + value + " &7to group &a" + groupName);
                                return;
                            }
                            case "minonlinecount" -> group.minServices(Integer.parseInt(value));
                            case "maxonlinecount" -> group.maxServices(Integer.parseInt(value));
                            case "maxplayers" -> group.maxPlayers(Integer.parseInt(value));
                            case "maxmemory" -> group.maxMemory(Integer.parseInt(value));
                            case "fallback" -> group.fallback(Boolean.parseBoolean(value));
                            case "startpercentage" -> group.startPercentage(Integer.parseInt(value));
                            case "startpriority" -> group.startPriority(Integer.parseInt(value));

                            default -> sendHelp();
                        }
                    } catch (Exception e) {
                        logger.info("&cInvalid value &a" + value + " &cfor key &a" + key);
                        return;
                    }

                    groupManager.update(group);
                    logger.info("Updated &a" + key + " &7for group &a" + groupName + "&7 to &a" + value);
                });
    }
}
