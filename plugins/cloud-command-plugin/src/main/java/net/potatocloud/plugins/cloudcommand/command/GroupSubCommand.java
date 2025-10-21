package net.potatocloud.plugins.cloudcommand.command;

import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.core.utils.PropertyUtil;
import net.potatocloud.plugins.cloudcommand.MessagesConfig;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class GroupSubCommand {

    private final Player player;
    private final MessagesConfig messages;

    public void listGroups() {
        final List<ServiceGroup> groups = CloudAPI.getInstance().getServiceGroupManager().getAllServiceGroups();
        player.sendMessage(messages.get("group.list.header"));
        for (ServiceGroup group : groups) {
            player.sendMessage(messages.get("group.list.entry")
                    .replaceText(text -> text.match("%name%").replacement(group.getName())));
        }
    }

    public void infoGroup(String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.get("group.info.usage"));
            return;
        }

        final String name = args[2];
        var groupManager = CloudAPI.getInstance().getServiceGroupManager();

        if (!groupManager.existsServiceGroup(name)) {
            player.sendMessage(messages.get("group.not-found")
                    .replaceText(text -> text.match("%name%").replacement(name)));
            return;
        }

        final ServiceGroup group = groupManager.getServiceGroup(name);

        player.sendMessage(messages.get("group.info.name").replaceText(text -> text.match("%name%").replacement(name)));
        player.sendMessage(messages.get("group.info.platform")
                .replaceText(text -> text.match("%platform%").replacement(group.getPlatform().getName())));
        player.sendMessage(messages.get("group.info.templates")
                .replaceText(text -> text.match("%templates%").replacement(String.join(", ", group.getServiceTemplates()))));
        player.sendMessage(messages.get("group.info.min-online")
                .replaceText(text -> text.match("%minOnline%").replacement(String.valueOf(group.getMinOnlineCount()))));
        player.sendMessage(messages.get("group.info.max-online")
                .replaceText(text -> text.match("%maxOnline%").replacement(String.valueOf(group.getMaxOnlineCount()))));
        player.sendMessage(messages.get("group.info.online-players")
                .replaceText(text -> text.match("%onlinePlayers%")
                        .replacement(String.valueOf(CloudAPI.getInstance().getPlayerManager().getOnlinePlayersByGroup(group).size()))));
        player.sendMessage(messages.get("group.info.max-players")
                .replaceText(text -> text.match("%maxPlayers%").replacement(String.valueOf(group.getMaxPlayers()))));
        player.sendMessage(messages.get("group.info.fallback")
                .replaceText(text -> text.match("%fallback%").replacement(MiniMessage.miniMessage().deserialize(group.isFallback() ? "<green>Yes" : "<red>No"))));
        player.sendMessage(messages.get("group.info.static")
                .replaceText(text -> text.match("%static%").replacement(MiniMessage.miniMessage().deserialize((group.isStatic() ? "<green>Yes" : "<red>No")))));
    }

    public void shutdownGroup(String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.get("group.shutdown.usage"));
            return;
        }

        final String name = args[2];
        final ServiceGroupManager groupManager = CloudAPI.getInstance().getServiceGroupManager();

        if (!groupManager.existsServiceGroup(name)) {
            player.sendMessage(messages.get("group.not-found")
                    .replaceText(text -> text.match("%name%").replacement(name)));
            return;
        }

        final ServiceGroup group = groupManager.getServiceGroup(name);
        for (Service service : group.getOnlineServices()) {
            service.shutdown();
        }
        player.sendMessage(messages.get("group.shutdown.success")
                .replaceText(text -> text.match("%name%").replacement(name)));
    }

    public void propertyGroup(String[] args) {
        if (args.length < 4) {
            player.sendMessage(messages.get("group.property.usage"));
            return;
        }

        final String sub = args[2].toLowerCase();
        final String name = args[3];
        final ServiceGroupManager groupManager = CloudAPI.getInstance().getServiceGroupManager();

        if (!groupManager.existsServiceGroup(name)) {
            player.sendMessage(messages.get("group.not-found")
                    .replaceText(text -> text.match("%name%").replacement(name)));
            return;
        }

        final ServiceGroup group = groupManager.getServiceGroup(name);

        switch (sub) {
            case "list" -> {
                final List<Property<?>> props = group.getProperties();

                if (props.isEmpty()) {
                    player.sendMessage(messages.get("group.property.empty")
                            .replaceText(text -> text.match("%name%").replacement(name)));
                    return;
                }

                player.sendMessage(messages.get("group.property.list.header")
                        .replaceText(text -> text.match("%name%").replacement(name)));

                for (Property<?> property : props) {
                    player.sendMessage(messages.get("group.property.list.entry")
                            .replaceText(text -> text.match("%key%").replacement(property.getName()))
                            .replaceText(text -> text.match("%value%").replacement(String.valueOf(property.getValue()))));
                }
            }

            case "remove" -> {
                if (args.length < 5) {
                    player.sendMessage(messages.get("group.property.remove.usage"));
                    return;
                }

                final String key = args[4];
                final Property<?> property = group.getProperty(key);

                if (property == null) {
                    player.sendMessage(messages.get("group.property.not-found")
                            .replaceText(text -> text.match("%key%").replacement(key)));
                    return;
                }

                group.getPropertyMap().remove(property.getName());
                group.update();

                player.sendMessage(messages.get("group.property.remove.success")
                        .replaceText(text -> text.match("%name%").replacement(name))
                        .replaceText(text -> text.match("%key%").replacement(key)));
            }

            case "set" -> {
                if (args.length < 6) {
                    player.sendMessage(messages.get("group.property.set.usage"));
                    return;
                }

                final String key = args[4];
                final String value = args[5];

                try {
                    final Property<?> property = PropertyUtil.stringToProperty(key, value);
                    group.setProperty(property);
                    group.update();

                    player.sendMessage(messages.get("group.property.set.success")
                            .replaceText(text -> text.match("%key%").replacement(key))
                            .replaceText(text -> text.match("%value%").replacement(value))
                            .replaceText(text -> text.match("%name%").replacement(name)));
                } catch (Exception e) {
                    player.sendMessage(messages.get("group.property.set.usage"));
                }
            }

            default -> player.sendMessage(messages.get("group.property.usage"));
        }
    }

    public void editGroup(String[] args) {
        if (args.length < 5) {
            player.sendMessage(messages.get("group.edit.usage"));
            return;
        }

        final String name = args[2];
        final String key = args[3].toLowerCase();
        final String value = args[4];

        final ServiceGroupManager groupManager = CloudAPI.getInstance().getServiceGroupManager();

        if (!groupManager.existsServiceGroup(name)) {
            player.sendMessage(messages.get("group.not-found")
                    .replaceText(text -> text.match("%name%").replacement(name)));
            return;
        }

        final ServiceGroup group = groupManager.getServiceGroup(name);

        try {
            switch (key) {
                case "addtemplate" -> {
                    group.addServiceTemplate(value);
                    group.update();
                    player.sendMessage(messages.get("group.edit.template.add")
                            .replaceText(text -> text.match("%template%").replacement(value)));
                    return;
                }
                case "removetemplate" -> {
                    if (group.getServiceTemplates().removeIf(s -> s.equalsIgnoreCase(value))) {
                        group.update();
                        player.sendMessage(messages.get("group.edit.template.remove")
                                .replaceText(text -> text.match("%template%").replacement(value)));
                    } else {
                        player.sendMessage(messages.get("group.edit.template.not-found")
                                .replaceText(text -> text.match("%template%").replacement(value)));
                    }
                    return;
                }
                case "addjvmflag" -> {
                    group.addCustomJvmFlag(value);
                    group.update();
                    player.sendMessage(messages.get("group.edit.jvmflag.add")
                            .replaceText(text -> text.match("%%flag%%").replacement(value)));
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
                    player.sendMessage(messages.get("group.edit.usage"));
                    return;
                }
            }
            group.update();
            player.sendMessage(messages.get("group.edit.success")
                    .replaceText(text -> text.match("%key%").replacement(key))
                    .replaceText(text -> text.match("%value%").replacement(value))
                    .replaceText(text -> text.match("%name%").replacement(name)));
        } catch (NumberFormatException e) {
            player.sendMessage(messages.get("group.edit.usage"));
        }
    }

    public List<String> suggest(String[] args) {
        if (args.length == 2) {
            return List.of("list", "info", "edit", "property", "shutdown").stream()
                    .filter(input -> input.startsWith(args[1].toLowerCase()))
                    .toList();
        }

        final String sub = args[1].toLowerCase();
        final ServiceGroupManager groupManager = CloudAPI.getInstance().getServiceGroupManager();

        if ((sub.equals("info") || sub.equals("edit") || sub.equals("shutdown"))) {
            if (args.length == 3) {
                return groupManager.getAllServiceGroups().stream()
                        .map(ServiceGroup::getName)
                        .filter(name -> name.startsWith(args[2]))
                        .toList();
            }
        }

        if (sub.equals("edit") && args.length == 4) {
            return List.of("minOnlineCount", "maxOnlineCount", "maxPlayers", "maxMemory", "fallback", "startPercentage", "startPriority", "addTemplate", "removeTemplate", "addJvmFlag")
                    .stream()
                    .filter(key -> key.startsWith(args[3].toLowerCase()))
                    .toList();
        }

        if (sub.equals("property")) {
            if (args.length == 3) {
                return List.of("list", "set", "remove").stream()
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .toList();
            }

            if (args.length == 4) {
                return groupManager.getAllServiceGroups().stream()
                        .map(ServiceGroup::getName)
                        .filter(name -> name.startsWith(args[3]))
                        .toList();
            }

            if (args.length == 5 && args[2].equalsIgnoreCase("remove")) {
                final String groupName = args[3];
                if (groupManager.existsServiceGroup(groupName)) {
                    return groupManager.getServiceGroup(groupName).getProperties().stream()
                            .map(Property::getName)
                            .filter(p -> p.startsWith(args[4]))
                            .toList();
                }
            }

            if (args.length == 5 && args[2].equalsIgnoreCase("set")) {
                List<String> completions = new ArrayList<>();
                completions.add("<custom>");
                completions.addAll(DefaultProperties.asSet().stream()
                        .map(Property::getName)
                        .filter(s -> s.startsWith(args[4].toLowerCase()))
                        .toList());
                return completions;
            }
        }

        return List.of();
    }

    public void sendHelpGroup(Player player) {
        player.sendMessage(messages.get("group.help.list"));
        player.sendMessage(messages.get("group.help.info"));
        player.sendMessage(messages.get("group.help.shutdown"));
        player.sendMessage(messages.get("group.help.edit"));
        player.sendMessage(messages.get("group.help.edit-addTemplate"));
        player.sendMessage(messages.get("group.help.edit-removeTemplate"));
        player.sendMessage(messages.get("group.help.edit-addJvmFlag"));
        player.sendMessage(messages.get("group.help.property"));
    }
}
