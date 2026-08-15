package net.potatocloud.plugins.optional.cloudcommand.command;

import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.api.service.Service;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.plugins.shared.MessagesConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class GroupSubCommand {

    private final Player player;
    private final MessagesConfig messages;

    public void listGroups() {
        final List<Group> groups = CloudAPI.instance().groupManager().groups();
        player.sendMessage(messages.get("group.list.header"));
        for (Group group : groups) {
            player.sendMessage(messages.get("group.list.entry").replaceText(text -> text.match("%name%").replacement(group.name())));
        }
    }

    public void infoGroup(String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.get("group.info.usage"));
            return;
        }

        final String name = args[2];

        CloudAPI.instance().groupManager().find(name).ifPresentOrElse(group -> {
            player.sendMessage(messages.get("group.info.name").replaceText(text -> text.match("%name%").replacement(name)));
            player.sendMessage(messages.get("group.info.platform").replaceText(text -> text.match("%platform%").replacement(group.platform().name())));
            player.sendMessage(messages.get("group.info.templates").replaceText(text -> text.match("%templates%").replacement(String.join(", ", group.templates()))));
            player.sendMessage(messages.get("group.info.min-online").replaceText(text -> text.match("%minOnline%").replacement(String.valueOf(group.minServices()))));
            player.sendMessage(messages.get("group.info.max-online").replaceText(text -> text.match("%maxOnline%").replacement(String.valueOf(group.maxServices()))));
            player.sendMessage(messages.get("group.info.online-players").replaceText(text -> text.match("%onlinePlayers%").replacement(String.valueOf(group.services().size()))));
            player.sendMessage(messages.get("group.info.max-players").replaceText(text -> text.match("%maxPlayers%").replacement(String.valueOf(group.maxPlayers()))));
            player.sendMessage(messages.get("group.info.fallback").replaceText(text -> text.match("%fallback%").replacement(MiniMessage.miniMessage().deserialize(group.fallback() ? "<green>Yes" : "<red>No"))));
            player.sendMessage(messages.get("group.info.static").replaceText(text -> text.match("%static%").replacement(MiniMessage.miniMessage().deserialize(group.staticServices() ? "<green>Yes" : "<red>No"))));
        }, () -> player.sendMessage(messages.get("group.not-found").replaceText(text -> text.match("%name%").replacement(name))));
    }

    public void shutdownGroup(String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.get("group.shutdown.usage"));
            return;
        }

        final String name = args[2];

        CloudAPI.instance().groupManager().find(name).ifPresentOrElse(group -> {
            group.services().stream().filter(Service::running).forEach(service -> CloudAPI.instance().serviceManager().stop(service));
            player.sendMessage(messages.get("group.shutdown.success").replaceText(text -> text.match("%name%").replacement(name)));
        }, () -> player.sendMessage(messages.get("group.not-found").replaceText(text -> text.match("%name%").replacement(name))));
    }

    public void propertyGroup(String[] args) {
        if (args.length < 4) {
            player.sendMessage(messages.get("group.property.usage"));
            return;
        }

        final String sub = args[2].toLowerCase();
        final String name = args[3];
        final GroupManager groupManager = CloudAPI.instance().groupManager();

        groupManager.find(name).ifPresentOrElse(group -> {
            switch (sub) {
                case "list" -> {
                    final Map<PropertyKey<?>, Object> properties = group.properties();

                    if (properties.isEmpty()) {
                        player.sendMessage(messages.get("group.property.empty").replaceText(text -> text.match("%name%").replacement(name)));
                        return;
                    }

                    player.sendMessage(messages.get("group.property.list.header").replaceText(text -> text.match("%name%").replacement(name)));

                    for (Map.Entry<PropertyKey<?>, Object> entry : properties.entrySet()) {
                        player.sendMessage(messages.get("group.property.list.entry").replaceText(text -> text.match("%key%").replacement(entry.getKey().name())).replaceText(text -> text.match("%value%").replacement(String.valueOf(entry.getValue()))));
                    }
                }

                case "remove" -> {
                    if (args.length < 5) {
                        player.sendMessage(messages.get("group.property.remove.usage"));
                        return;
                    }

                    final String key = args[4];

                    final Optional<PropertyKey<?>> propertyKey = group.get(key);

                    if (propertyKey.isEmpty()) {
                        player.sendMessage(messages.get("group.property.not-found").replaceText(text -> text.match("%key%").replacement(key)));
                        return;
                    }

                    group.removeProperty(propertyKey.get());
                    groupManager.update(group);
                    player.sendMessage(messages.get("group.property.remove.success").replaceText(text -> text.match("%name%").replacement(name)).replaceText(text -> text.match("%key%").replacement(key)));
                }

                case "set" -> {
                    if (args.length < 6) {
                        player.sendMessage(messages.get("group.property.set.usage"));
                        return;
                    }

                    final String key = args[4];
                    final String value = args[5];

                    try {
                        PropertyUtil.setString(group, key, value);
                        groupManager.update(group);

                        player.sendMessage(messages.get("group.property.set.success").replaceText(text -> text.match("%key%").replacement(key)).replaceText(text -> text.match("%value%").replacement(value)).replaceText(text -> text.match("%name%").replacement(name)));
                    } catch (Exception e) {
                        player.sendMessage(messages.get("group.property.set.usage"));
                    }
                }

                default -> player.sendMessage(messages.get("group.property.usage"));
            }
        }, () -> player.sendMessage(messages.get("group.not-found").replaceText(text -> text.match("%name%").replacement(name))));
    }

    public void editGroup(String[] args) {
        if (args.length < 5) {
            player.sendMessage(messages.get("group.edit.usage"));
            return;
        }

        final String name = args[2];
        final String key = args[3].toLowerCase();
        final String value = args[4];

        final GroupManager groupManager = CloudAPI.instance().groupManager();

        groupManager.find(name).ifPresentOrElse(group -> {
            try {
                switch (key) {
                    case "addtemplate" -> {
                        group.addTemplate(value);
                        groupManager.update(group);
                        player.sendMessage(messages.get("group.edit.template.add").replaceText(text -> text.match("%template%").replacement(value)));
                        return;
                    }
                    case "removetemplate" -> {
                        if (group.templates().removeIf(template -> template.equalsIgnoreCase(value))) {
                            groupManager.update(group);
                            player.sendMessage(messages.get("group.edit.template.remove").replaceText(text -> text.match("%template%").replacement(value)));
                        } else {
                            player.sendMessage(messages.get("group.edit.template.not-found").replaceText(text -> text.match("%template%").replacement(value)));
                        }
                        return;
                    }
                    case "addjvmflag" -> {
                        group.addCustomJvmFlag(value);
                        groupManager.update(group);
                        player.sendMessage(messages.get("group.edit.jvmflag.add").replaceText(text -> text.match("%%flag%%").replacement(value)));
                        return;
                    }
                    case "minonlinecount" -> group.minServices(Integer.parseInt(value));
                    case "maxonlinecount" -> group.maxServices(Integer.parseInt(value));
                    case "maxplayers" -> group.maxPlayers(Integer.parseInt(value));
                    case "maxmemory" -> group.maxMemory(Integer.parseInt(value));
                    case "fallback" -> group.fallback(Boolean.parseBoolean(value));
                    case "startpercentage" -> group.startPercentage(Integer.parseInt(value));
                    case "startpriority" -> group.startPriority(Integer.parseInt(value));
                    default -> {
                        player.sendMessage(messages.get("group.edit.usage"));
                        return;
                    }
                }
                groupManager.update(group);
                player.sendMessage(messages.get("group.edit.success").replaceText(text -> text.match("%key%").replacement(key)).replaceText(text -> text.match("%value%").replacement(value)).replaceText(text -> text.match("%name%").replacement(name)));
            } catch (NumberFormatException e) {
                player.sendMessage(messages.get("group.edit.usage"));
            }
        }, () -> player.sendMessage(messages.get("group.not-found").replaceText(text -> text.match("%name%").replacement(name))));
    }

    public List<String> suggest(String[] args) {
        if (args.length == 2) {
            return List.of("list", "info", "edit", "property", "shutdown").stream().filter(input -> input.startsWith(args[1].toLowerCase())).toList();
        }

        final String sub = args[1].toLowerCase();
        final GroupManager groupManager = CloudAPI.instance().groupManager();

        if (sub.equals("info") || sub.equals("edit") || sub.equals("shutdown")) {
            if (args.length == 3) {
                return groupManager.groups().stream()
                        .map(Group::name)
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
                        .filter(option -> option.startsWith(args[2].toLowerCase()))
                        .toList();
            }

            if (args.length == 4) {
                return groupManager.groups().stream()
                        .map(Group::name).filter(name -> name.startsWith(args[3]))
                        .toList();
            }

            if (args.length == 5 && args[2].equalsIgnoreCase("remove")) {
                return groupManager.find(args[3])
                        .map(group -> group.properties().keySet().stream().map(PropertyKey::name)
                                .filter(propertyName -> propertyName.startsWith(args[4]))
                                .toList())
                        .orElseGet(List::of);
            }

            if (args.length == 5 && args[2].equalsIgnoreCase("set")) {
                List<String> completions = new ArrayList<>();
                completions.add("<custom>");
                completions.addAll(DefaultProperties.values().stream()
                        .map(PropertyKey::name)
                        .filter(propertyName -> propertyName.startsWith(args[4].toLowerCase())).
                        toList());
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
