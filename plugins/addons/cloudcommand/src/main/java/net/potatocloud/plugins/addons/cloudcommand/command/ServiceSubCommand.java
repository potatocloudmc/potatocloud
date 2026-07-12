package net.potatocloud.plugins.addons.cloudcommand.command;

import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.utils.TimeFormatter;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.plugins.shared.MessagesConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class ServiceSubCommand {

    private final Player player;
    private final MessagesConfig messages;
    private final ServiceManager serviceManager = CloudAPI.instance().serviceManager();

    public void sendHelp(Player player) {
        player.sendMessage(messages.get("service.help.list"));
        player.sendMessage(messages.get("service.help.start"));
        player.sendMessage(messages.get("service.help.stop"));
        player.sendMessage(messages.get("service.help.info"));
        player.sendMessage(messages.get("service.help.edit"));
        player.sendMessage(messages.get("service.help.property"));
        player.sendMessage(messages.get("service.help.copy"));
    }

    public void listServices() {
        final List<Service> services = serviceManager.services();
        player.sendMessage(messages.get("service.list.header"));
        for (Service service : services) {
            player.sendMessage(messages.get("service.list.entry")
                    .replaceText(text -> text.match("%name%").replacement(service.name()))
                    .replaceText(text -> text.match("%group%").replacement(service.group().name()))
                    .replaceText(text -> text.match("%state%").replacement(service.state().name())));
        }
    }

    public void startService(String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.get("service.start.usage"));
            return;
        }

        final String groupName = args[2];
        if (!CloudAPI.instance().groupManager().exists(groupName)) {
            player.sendMessage(messages.get("no-group").replaceText(text -> text.match("%name%").replacement(groupName)));
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount <= 0) {
                    player.sendMessage(messages.get("service.start.usage"));
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(messages.get("service.start.usage"));
            }
        }

        for (int i = 0; i < amount; i++) {
            CloudAPI.instance().groupManager().find(groupName).ifPresent(serviceManager::start);
        }

        int finalAmount = amount;
        player.sendMessage(messages.get("service.start.starting")
                .replaceText(text -> text.match("%amount%").replacement(String.valueOf(finalAmount)))
                .replaceText(text -> text.match("%group%").replacement(groupName)));
    }

    public void stopService(String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.get("service.stop.usage"));
            return;
        }

        final String name = args[2];

        serviceManager.find(name).ifPresentOrElse(serviceManager::stop, () -> player.sendMessage(messages.get("no-service")));
    }

    public void infoService(String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.get("service.info.usage"));
            return;
        }

        final String name = args[2];

        serviceManager.find(name).ifPresentOrElse(service -> {
            player.sendMessage(messages.get("service.info.name").replaceText(text -> text.match("%name%").replacement(service.name())));
            player.sendMessage(messages.get("service.info.group").replaceText(text -> text.match("%group%").replacement(service.group().name())));
            player.sendMessage(messages.get("service.info.port").replaceText(text -> text.match("%port%").replacement(String.valueOf(service.port()))));
            player.sendMessage(messages.get("service.info.state").replaceText(text -> text.match("%state%").replacement(service.state().name())));
            player.sendMessage(messages.get("service.info.online-players").replaceText(text -> text.match("%players%").replacement(String.valueOf(service.playerCount()))));
            player.sendMessage(messages.get("service.info.max-players").replaceText(text -> text.match("%maxPlayers%").replacement(String.valueOf(service.maxPlayers()))));
            player.sendMessage(messages.get("service.info.online-time").replaceText(text -> text.match("%onlineTime%").replacement(TimeFormatter.formatAsDuration(service.uptime().toMillis()))));
            player.sendMessage(messages.get("service.info.start-time").replaceText(text -> text.match("%startTime%").replacement(TimeFormatter.formatAsDateAndTime(service.startedAt().toEpochMilli()))));
            player.sendMessage(messages.get("service.info.used-memory").replaceText(text -> text.match("%usedMemory%").replacement(String.valueOf(service.usedMemory()))));
        }, () -> player.sendMessage(messages.get("no-service")));
    }

    public void editService(String[] args) {
        if (args.length < 5) {
            player.sendMessage(messages.get("service.edit.usage"));
            return;
        }

        final String name = args[2];
        serviceManager.find(name).ifPresentOrElse(service -> {
            final String key = args[3].toLowerCase();
            final String value = args[4];

            try {
                if (key.equals("maxplayers")) {
                    service.maxPlayers(Integer.parseInt(value));
                } else {
                    player.sendMessage(messages.get("service.edit.usage"));
                    return;
                }
                serviceManager.update(service);
                player.sendMessage(messages.get("service.edit.success")
                        .replaceText(text -> text.match("%key%").replacement(key))
                        .replaceText(text -> text.match("%value%").replacement(value))
                        .replaceText(text -> text.match("%name%").replacement(name)));
            } catch (NumberFormatException e) {
                player.sendMessage(messages.get("service.edit.usage"));
            }
        }, () -> player.sendMessage(messages.get("no-service")));
    }

    public void propertyService(String[] args) {
        if (args.length < 4) {
            player.sendMessage(messages.get("service.property.usage"));
            return;
        }

        final String sub = args[2].toLowerCase();
        final String name = args[3];

        final Optional<Service> service = serviceManager.find(name);
        if (service.isEmpty()) {
            player.sendMessage(messages.get("no-service"));
            return;
        }

        switch (sub) {
            case "list" -> {
                final Map<PropertyKey<?>, Object> properties = service.get().properties();

                if (properties.isEmpty()) {
                    player.sendMessage(messages.get("service.property.empty")
                            .replaceText(text -> text.match("%name%").replacement(name)));
                    return;
                }

                player.sendMessage(messages.get("service.property.list.header")
                        .replaceText(text -> text.match("%name%").replacement(name)));

                for (Map.Entry<PropertyKey<?>, Object> entry : properties.entrySet()) {
                    player.sendMessage(messages.get("service.property.list.entry")
                            .replaceText(text -> text.match("%key%").replacement(entry.getKey().name()))
                            .replaceText(text -> text.match("%value%").replacement(String.valueOf(entry.getValue()))));
                }
            }

            case "remove" -> {
                if (args.length < 5) {
                    player.sendMessage(messages.get("service.property.remove.usage"));
                    return;
                }

                final String key = args[4];

                final Optional<PropertyKey<?>> propertyKey = service.get().get(key);

                if (propertyKey.isEmpty()) {
                    player.sendMessage(messages.get("service.property.not-found").replaceText(text -> text.match("%key%").replacement(key)));
                    return;
                }

                service.get().removeProperty(propertyKey.get());
                serviceManager.update(service.get());
                player.sendMessage(messages.get("service.property.remove.success")
                        .replaceText(text -> text.match("%name%").replacement(name))
                        .replaceText(text -> text.match("%key%").replacement(key)));
            }

            case "set" -> {
                if (args.length < 6) {
                    player.sendMessage(messages.get("service.property.set.usage"));
                    return;
                }

                final String key = args[4];
                final String value = args[5];

                try {
                    PropertyUtil.setString(service.get(), key, value);
                    serviceManager.update(service.get());

                    player.sendMessage(messages.get("service.property.set.success")
                            .replaceText(text -> text.match("%key%").replacement(key))
                            .replaceText(text -> text.match("%value%").replacement(value))
                            .replaceText(text -> text.match("%name%").replacement(name)));
                } catch (Exception e) {
                    player.sendMessage(messages.get("service.property.set.usage"));
                }
            }

            default -> player.sendMessage(messages.get("service.property.usage"));
        }
    }

    public void copyService(String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.get("service.copy.usage"));
            return;
        }

        final String name = args[2];
        serviceManager.find(name).ifPresentOrElse(service -> {
            final String template = args[3];

            String filter;
            if (args.length >= 5) {
                filter = args[4];
            } else {
                filter = "";
            }

            if (filter.isEmpty()) {
                serviceManager.copyTo(service, template);
            } else {
                serviceManager.copyTo(service, template, filter);
            }

            player.sendMessage(messages.get("service.copy.success")
                    .replaceText(text -> text.match("%files%").replacement(filter.isEmpty() ? "all service files" : filter))
                    .replaceText(text -> text.match("%template%").replacement(template)));
        }, () -> player.sendMessage(messages.get("no-service")));
    }

    public List<String> suggest(String[] args) {
        if (args.length < 2) {
            return List.of();
        }

        if (args.length == 2) {
            return List.of("list", "start", "stop", "info", "edit", "property", "copy").stream()
                    .filter(input -> input.startsWith(args[1].toLowerCase()))
                    .toList();
        }

        String sub = args[1].toLowerCase();

        if (List.of("stop", "info", "edit", "copy").contains(sub) && args.length == 3) {
            return serviceManager.services().stream()
                    .map(Service::name)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
        }


        if (sub.equalsIgnoreCase("copy") && args.length == 4) {
            return serviceManager.find(args[2])
                    .map(service -> service.group()
                            .templates()
                            .stream()
                            .toList())
                    .orElse(List.of());
        }

        if (sub.equalsIgnoreCase("start") && args.length == 3) {
            return CloudAPI.instance().groupManager().groups().stream().map(Group::name).toList();
        }


        if (sub.equals("edit") && args.length == 4) {
            return List.of("maxPlayers").stream()
                    .filter(key -> key.toLowerCase().startsWith(args[3].toLowerCase()))
                    .toList();
        }

        if (sub.equals("property")) {
            if (args.length == 3) {
                return List.of("list", "set", "remove").stream()
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .toList();
            }

            if (args.length == 4) {
                return serviceManager.services().stream()
                        .map(Service::name)
                        .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                        .toList();
            }

            if (args.length == 5 && args[2].equalsIgnoreCase("remove")) {
                return serviceManager.find(args[3])
                        .map(service -> service.properties().keySet().stream()
                                .map(PropertyKey::name)
                                .filter(p -> p.toLowerCase().startsWith(args[4].toLowerCase()))
                                .toList())
                        .orElse(List.of());
            }

            if (args.length == 5 && args[2].equalsIgnoreCase("set")) {
                List<String> completions = new ArrayList<>();
                completions.add("<custom>");
                completions.addAll(DefaultProperties.values().stream()
                        .map(PropertyKey::name)
                        .filter(name -> name.toLowerCase().startsWith(args[4].toLowerCase()))
                        .toList());
                return completions;
            }
        }

        return List.of();
    }
}
