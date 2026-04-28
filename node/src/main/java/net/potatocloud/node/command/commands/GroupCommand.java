package net.potatocloud.node.command.commands;

import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
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

    public GroupCommand(Logger logger, ServiceGroupManager groupManager) {
        final Node node = Node.getInstance();

        defaultExecutor(ctx -> sendHelp());

        sub("create", "Create a new group")
                .executes(ctx -> {
                    node.getSetupManager().startSetup(new GroupConfigurationSetup(
                            node.getConsole(),
                            node.getScreenManager(),
                            groupManager,
                            node.getPlatformManager())
                    );
                });

        sub("delete", "Delete a group")
                .argument(ArgumentType.Group("group"))
                .executes(ctx -> {
                    final ServiceGroup group = ctx.get("group");

                    groupManager.deleteServiceGroup(group);
                    logger.info("&7Group &a" + group.getName() + " &7was deleted");
                });

        sub("list", "List all groups")
                .executes(ctx -> {
                    final List<ServiceGroup> groups = groupManager.getAllServiceGroups();

                    if (groups.isEmpty()) {
                        logger.info("There are &cno &7groups");
                        return;
                    }

                    logger.info("Loaded groups&8:");
                    for (ServiceGroup group : groups) {
                        logger.info("&8» &a" + group.getName());
                    }
                });

        sub("info", "Show details of a group")
                .argument(ArgumentType.Group("group"))
                .executes(ctx -> {
                    final ServiceGroup group = ctx.get("group");

                    logger.info("&7Info for group &a" + group.getName() + "&8:");
                    logger.info("&8» &7Platform: &a" + group.getPlatform().getName());
                    logger.info("&8» &7Version: &a" + group.getPlatformVersion().getName());
                    logger.info("&8» &7Templates: &a" + String.join(", ", group.getServiceTemplates()));
                    logger.info("&8» &7Min Online Count: &a" + group.getMinOnlineCount());
                    logger.info("&8» &7Max Online Count: &a" + group.getMaxOnlineCount());
                    logger.info("&8» &7Online Players: &a" + node.getPlayerManager().getOnlinePlayersByGroup(group).size());
                    logger.info("&8» &7Max Players: &a" + group.getMaxPlayers());
                    logger.info("&8» &7Max Memory: &a" + group.getMaxMemory() + "MB");
                    logger.info("&8» &7Primary: " + (group.isPrimary() ? "&aYes" : "&cNo"));
                    logger.info("&8» &7Static: " + (group.isStatic() ? "&aYes" : "&cNo"));
                });

        sub("stop", "Stop all services in a group")
                .argument(ArgumentType.Group("group"))
                .executes(ctx -> {
                    final ServiceGroup group = ctx.get("group");

                    for (Service service : group.getAllServices()) {
                        service.shutdown();
                    }
                });

        final SubCommand propertySub = sub("property", "Manage properties of a group");

        propertySub.executes(ctx -> propertySub.sendHelp());

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
                        suggestions.add(property.getName());
                    }

                    suggestions.add("<custom>");

                    return suggestions.stream()
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final ServiceGroup group = ctx.get("group");
                    final String key = ctx.get("key");
                    final String value = ctx.get("value");

                    try {
                        final Property<?> property = PropertyUtil.stringToProperty(key, value);

                        group.setProperty(property);
                        group.update();
                        logger.info("Property &a" + key + " &7was set to &a" + value + " &7in group &a" + group.getName());
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

                    final ServiceGroup group = ctx.get("group");

                    return group.getProperties().stream()
                            .map(Property::getName)
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final ServiceGroup group = ctx.get("group");
                    final String key = ctx.get("key");

                    final Property<?> property = group.getProperty(key);
                    if (property == null) {
                        logger.info("Property &a" + key + "&7 was &cnot found &7in group &a" + group.getName());
                        return;
                    }

                    group.getPropertyMap().remove(property.getName());
                    group.update();
                    logger.info("Property &a" + key + " &7was removed in group &a" + group.getName());
                });

        propertySub.sub("list")
                .argument(ArgumentType.Group("group"))
                .executes(ctx -> {
                    final ServiceGroup group = ctx.get("group");
                    final List<Property<?>> properties = group.getProperties();

                    if (properties.isEmpty()) {
                        logger.info("No properties found for group &a" + group.getName());
                        return;
                    }

                    logger.info("Properties of group &a" + group.getName() + "&8:");
                    for (Property<?> property : properties) {
                        logger.info("&8» &a" + property.getName() + " &7- " + property.getValue());
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
                            "primary",
                            "startPercentage",
                            "startPriority"
                    );

                    return suggestions.stream()
                            .filter(name -> name.startsWith(input))
                            .toList();
                })
                .executes(ctx -> {
                    final ServiceGroup group = ctx.get("group");
                    String key = ctx.get("key");
                    final String value = ctx.get("value");

                    key = key.toLowerCase();

                    final String groupName = group.getName();

                    try {
                        switch (key) {
                            case "addtemplate" -> {
                                group.addServiceTemplate(value);
                                Node.getInstance().getTemplateManager().createTemplate(value);
                                group.update();
                                logger.info("Template &a" + value + " &7was added to group &a" + groupName);
                                return;
                            }
                            case "removetemplate" -> {
                                if (group.getServiceTemplates().removeIf(s -> s.equalsIgnoreCase(value))) {
                                    group.update();
                                    logger.info("Template &a" + value + " &7was removed from group &a" + groupName);
                                } else {
                                    logger.info("Template &a" + value + " &7was not found in group &a" + groupName);
                                }
                                return;
                            }
                            case "addjvmflag" -> {
                                group.addCustomJvmFlag(value);
                                group.update();
                                logger.info("Added JVM flag &a" + value + " &7to group &a" + groupName);
                                return;
                            }
                            case "minonlinecount" -> group.setMinOnlineCount(Integer.parseInt(value));
                            case "maxonlinecount" -> group.setMaxOnlineCount(Integer.parseInt(value));
                            case "maxplayers" -> group.setMaxPlayers(Integer.parseInt(value));
                            case "maxmemory" -> group.setMaxMemory(Integer.parseInt(value));
                            case "primary" -> group.setPrimary(Boolean.parseBoolean(value));
                            case "startpercentage" -> group.setStartPercentage(Integer.parseInt(value));
                            case "startpriority" -> group.setStartPriority(Integer.parseInt(value));

                            default -> sendHelp();
                        }
                    } catch (Exception e) {
                        logger.info("&cInvalid value &a" + value + " &cfor key &a" + key);
                        return;
                    }

                    group.update();
                    logger.info("Updated &a" + key + " &7for group &a" + groupName + "&7 to &a" + value);
                });
    }
}
