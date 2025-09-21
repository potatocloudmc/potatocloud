package net.potatocloud.node.command.commands;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.TabCompleter;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.setup.setups.GroupConfigurationSetup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class GroupCommand implements Command, TabCompleter {

    private final Logger logger;
    private final ServiceGroupManager groupManager;

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            sendHelp();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> listGroups();
            case "create" -> createGroup(args);
            case "delete" -> deleteGroup(args);
            case "info" -> infoGroup(args);
            case "edit" -> editGroup(args);
            case "property", "properties" -> propertyGroup(args);
            case "shutdown" -> shutdownGroup(args);
            default -> sendHelp();
        }
    }

    private void shutdownGroup(String[] args) {
        if (args.length < 2) {
            logger.info("&cUsage&8: &7group shutdown [&aname&8] ");
            return;
        }

        final String name = args[1];
        if (!groupManager.existsServiceGroup(name)) {
            logger.info("&cNo service group found with the name &a" + name);
            return;
        }

        final ServiceGroup group = groupManager.getServiceGroup(name);
        for (Service service : group.getOnlineServices()) {
            service.shutdown();
        }
    }

    private void propertyGroup(String[] args) {
        if (args.length < 2) {
            logger.info("&cUsage&8: &7group property &8[&7list&8|&7set&8|&7remove&8] [&aname&8] [&akey&8] [&avalue&8]");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "list" -> {
                if (args.length < 3) {
                    logger.info("&cUsage&8: &7group property list &8[&aname&8]");
                    return;
                }

                final String name = args[2];
                if (!groupManager.existsServiceGroup(name)) {
                    logger.info("&cNo service group found with the name &a" + name);
                    return;
                }

                final ServiceGroup group = groupManager.getServiceGroup(name);
                final Set<Property> properties = group.getProperties();

                if (properties.isEmpty()) {
                    logger.info("No properties found for group &a" + name);
                    return;
                }

                logger.info("Properties of group &a" + name + "&8:");
                for (Property property : properties) {
                    logger.info("&8» &a" + property.getName() + " &7- " + property.getValue());
                }
            }
            case "remove" -> {
                if (args.length < 4) {
                    logger.info("&cUsage&8: &7group property remove &8[&aname&8] [&akey&8]");
                    return;
                }
                final String name = args[2];
                if (!groupManager.existsServiceGroup(name)) {
                    logger.info("&cNo service group found with the name &a" + name);
                    return;
                }

                final ServiceGroup group = groupManager.getServiceGroup(name);

                final String key = args[3].toLowerCase();
                final Property property = group.getProperty(key);
                if (property == null) {
                    logger.info("Property &a" + key + "&7 was &cnot found &7in group &a" + name);
                    return;
                }
                group.getProperties().remove(property);
                group.update();
                logger.info("Property &a" + key + " &7was removed in group &a" + name);
            }
            case "set" -> {
                if (args.length < 5) {
                    logger.info("&cUsage&8: &7group property set &8[&aname&8] [&akey&8] [&avalue&8]");
                    return;
                }

                final String name = args[2];
                if (!groupManager.existsServiceGroup(name)) {
                    logger.info("&cNo service group found with the name &a" + name);
                    return;
                }

                final ServiceGroup group = groupManager.getServiceGroup(name);
                final String key = args[3].toLowerCase();
                final String value = args[4];

                try {
                    group.setProperty(Property.of(key, value, value));
                    group.update();
                    logger.info("Property &a" + key + " &7was set to &a" + value + " &7in group &a" + name);
                } catch (Exception e) {
                    logger.info("&cUsage&8: &7group property set &8[&aname&8] [&akey&8] [&avalue&8]");
                }
            }
            default ->
                    logger.info("&cUsage&8: &7group property &8[&7list&8|&7set&8|&7remove&8] [&aname&8] [&akey&8] [&avalue&8]");
        }
    }

    private void editGroup(String[] args) {
        if (args.length < 4) {
            logger.info("&cUsage&8: &7group edit &8[&aname&8] [&akey&8] [&avalue&8]");
            return;
        }

        final String name = args[1];
        if (!groupManager.existsServiceGroup(name)) {
            logger.info("&cNo service group found with the name &a" + name);
            return;
        }

        final ServiceGroup group = groupManager.getServiceGroup(name);
        final String key = args[2].toLowerCase();
        final String value = args[3];

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
                    logger.info("&cUsage&8: &7group edit &8[&aname&8] [&akey&8] [&avalue&8]");
                    return;
                }
            }
            group.update();
            logger.info("Updated &a" + key + " &7for group &a" + name + "&7 to &a" + value);
        } catch (NumberFormatException ex) {
            logger.info("&cUsage&8: &7group edit &8[&aname&8] [&akey&8] [&avalue&8]");
        }
    }

    private void listGroups() {
        List<ServiceGroup> groups = groupManager.getAllServiceGroups();
        if (groups.isEmpty()) {
            logger.info("There are &cno &7service groups");
            return;
        }
        logger.info("All loaded groups&8:");
        for (ServiceGroup group : groups) {
            logger.info("&8» &a" + group.getName());
        }
    }

    private void createGroup(String[] args) {
        //todo
        Node.getInstance().getSetupManager().startSetup(new GroupConfigurationSetup(Node.getInstance().getConsole(), Node.getInstance().getScreenManager(), groupManager, Node.getInstance().getPlatformManager()));
    }

    private void deleteGroup(String[] args) {
        if (args.length < 2) {
            logger.info("&cUsage&8: &7group delete &8[&aname&8]");
            return;
        }

        final String name = args[1];
        if (!groupManager.existsServiceGroup(name)) {
            logger.info("&cNo service group found with the name &a" + name);
            return;
        }

        ServiceGroup group = groupManager.getServiceGroup(name);
        groupManager.deleteServiceGroup(group);
        logger.info("&7Service group &a" + name + " &7was deleted");
    }

    private void infoGroup(String[] args) {
        if (args.length < 2) {
            logger.info("&cUsage&8: &7group info &8[&aname&8]");
            return;
        }

        final String name = args[1];
        if (!groupManager.existsServiceGroup(name)) {
            logger.info("&cNo service group found with the name &a" + name);
            return;
        }

        final ServiceGroup group = groupManager.getServiceGroup(name);
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

    private void sendHelp() {
        logger.info("group create &8- &7Create a new service group");
        logger.info("group delete &8[&aname&8] - &7Delete the group");
        logger.info("group list &8- &7List all groups");
        logger.info("group info &8[&aname&8] - &7Show details of the group");
        logger.info("group shutdown &8[&aname&8] - &7Stop all services of the group");
        logger.info("group edit &8[&aname&8] [&akey&8] [&avalue&8] - &7Edit the group");
        logger.info("group edit &8[&aname&8] &7addTemplate &8[&atemplate&8] - &7Add a template to the group");
        logger.info("group edit &8[&aname&8] &7removeTemplate &8[&atemplate&8] - &7Remove a template from the group");
        logger.info("group edit &8[&aname&8] &7addJvmFlag &8[&aflag&8] - &7Add a custom JVM flag to the group");
        logger.info("group property &8[&7list&8|&7set&8|&7remove&8] [&aname&8] [&akey&8] [&avalue&8] - &7Manage properties of the group");
    }

    @Override
    public String getName() {
        return "group";
    }

    @Override
    public String getDescription() {
        return "Manage service groups";
    }

    @Override
    public List<String> getAliases() {
        return List.of("groups");
    }

    @Override
    public List<String> complete(String[] args) {
        if (args.length == 1) {
            return List.of("list", "create", "delete", "info", "edit", "property", "shutdown").stream()
                    .filter(input -> input.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        final String sub = args[0].toLowerCase();

        if ((sub.equals("info") || sub.equals("delete") || sub.equals("edit"))) {
            if (args.length == 2) {
                return groupManager.getAllServiceGroups().stream()
                        .map(ServiceGroup::getName)
                        .filter(name -> name.startsWith(args[1]))
                        .toList();
            }
        }

        if (sub.equals("edit") && args.length == 3) {
            return List.of("minOnlineCount", "maxOnlineCount", "maxPlayers", "maxMemory", "fallback", "startPercentage", "startPriority", "addTemplate", "removeTemplate", "addJvmFlag")
                    .stream()
                    .filter(key -> key.startsWith(args[2].toLowerCase()))
                    .toList();
        }

        if (sub.equals("property")) {
            if (args.length == 2) {
                return List.of("list", "set", "remove").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .toList();
            }

            if (args.length == 3) {
                return groupManager.getAllServiceGroups().stream()
                        .map(ServiceGroup::getName)
                        .filter(name -> name.startsWith(args[2]))
                        .toList();
            }

            if (args.length == 4 && args[1].equalsIgnoreCase("remove")) {
                final String groupName = args[2];
                if (groupManager.existsServiceGroup(groupName)) {
                    return groupManager.getServiceGroup(groupName).getProperties().stream()
                            .map(Property::getName)
                            .filter(p -> p.startsWith(args[3]))
                            .toList();
                }
            }

            if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
                final List<String> completions = new ArrayList<>();
                completions.add("<custom>");
                completions.addAll(Property.getDefaultProperties().stream()
                        .map(Property::getName)
                        .filter(s -> s.startsWith(args[3].toLowerCase()))
                        .toList());
                return completions;
            }
        }
        return List.of();
    }
}
