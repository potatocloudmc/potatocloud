package net.potatocloud.plugins.cloudcommand.command;

import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.core.utils.PropertyUtil;
import net.potatocloud.plugins.cloudcommand.MessagesConfig;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ServiceSubCommand {

    private final Player player;
    private final MessagesConfig messages;
    private final ServiceManager serviceManager = CloudAPI.getInstance().getServiceManager();

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
        final List<Service> services = serviceManager.getAllServices();
        player.sendMessage(messages.get("service.list.header"));
        for (Service service : services) {
            player.sendMessage(messages.get("service.list.entry")
                    .replaceText(text -> text.match("%name%").replacement(service.getName()))
                    .replaceText(text -> text.match("%group%").replacement(service.getServiceGroup().getName()))
                    .replaceText(text -> text.match("%status%").replacement(service.getStatus().name())));
        }
    }

    public void startService(String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.get("service.start.usage"));
            return;
        }

        final String groupName = args[2];
        if (!CloudAPI.getInstance().getServiceGroupManager().existsServiceGroup(groupName)) {
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

        serviceManager.startServices(CloudAPI.getInstance().getServiceGroupManager().getServiceGroup(groupName), amount);
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
        final Service service = serviceManager.getService(name);
        if (service == null) {
            player.sendMessage(messages.get("no-service"));
            return;
        }

        service.shutdown();
    }

    public void infoService(String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.get("service.info.usage"));
            return;
        }

        final String name = args[2];
        final Service service = serviceManager.getService(name);
        if (service == null) {
            player.sendMessage(messages.get("no-service"));
            return;
        }

        player.sendMessage(messages.get("service.info.name").replaceText(text -> text.match("%name%").replacement(service.getName())));
        player.sendMessage(messages.get("service.info.group").replaceText(text -> text.match("%group%").replacement(service.getServiceGroup().getName())));
        player.sendMessage(messages.get("service.info.port").replaceText(text -> text.match("%port%").replacement(String.valueOf(service.getPort()))));
        player.sendMessage(messages.get("service.info.status").replaceText(text -> text.match("%status%").replacement(service.getStatus().name())));
        player.sendMessage(messages.get("service.info.online-players").replaceText(text -> text.match("%players%").replacement(String.valueOf(service.getOnlinePlayerCount()))));
        player.sendMessage(messages.get("service.info.max-players").replaceText(text -> text.match("%maxPlayers%").replacement(String.valueOf(service.getMaxPlayers()))));
        player.sendMessage(messages.get("service.info.online-time").replaceText(text -> text.match("%onlineTime%").replacement(String.valueOf(service.getFormattedUptime()))));
        player.sendMessage(messages.get("service.info.start-time").replaceText(text -> text.match("%startTime%").replacement(String.valueOf(service.getFormattedStartTimestamp()))));
        player.sendMessage(messages.get("service.info.used-memory").replaceText(text -> text.match("%usedMemory%").replacement(String.valueOf(service.getUsedMemory()))));
    }

    public void editService(String[] args) {
        if (args.length < 5) {
            player.sendMessage(messages.get("service.edit.usage"));
            return;
        }

        final String name = args[2];
        final Service service = serviceManager.getService(name);
        if (service == null) {
            player.sendMessage(messages.get("no-service"));
            return;
        }

        final String key = args[3].toLowerCase();
        final String value = args[4];

        try {
            if (key.equals("maxplayers")) {
                service.setMaxPlayers(Integer.parseInt(value));
            } else {
                player.sendMessage(messages.get("service.edit.usage"));
                return;
            }
            service.update();
            player.sendMessage(messages.get("service.edit.success")
                    .replaceText(text -> text.match("%key%").replacement(key))
                    .replaceText(text -> text.match("%value%").replacement(value))
                    .replaceText(text -> text.match("%name%").replacement(name)));
        } catch (NumberFormatException e) {
            player.sendMessage(messages.get("service.edit.usage"));
        }
    }

    public void propertyService(String[] args) {
        if (args.length < 4) {
            player.sendMessage(messages.get("service.property.usage"));
            return;
        }

        final String sub = args[2].toLowerCase();
        final String name = args[3];

        final Service service = serviceManager.getService(name);
        if (service == null) {
            player.sendMessage(messages.get("no-service"));
            return;
        }

        switch (sub) {
            case "list" -> {
                final List<Property<?>> props = service.getProperties();

                if (props.isEmpty()) {
                    player.sendMessage(messages.get("service.property.empty")
                            .replaceText(text -> text.match("%name%").replacement(name)));
                    return;
                }

                player.sendMessage(messages.get("service.property.list.header")
                        .replaceText(text -> text.match("%name%").replacement(name)));

                for (Property<?> property : props) {
                    player.sendMessage(messages.get("service.property.list.entry")
                            .replaceText(text -> text.match("%key%").replacement(property.getName()))
                            .replaceText(text -> text.match("%value%").replacement(String.valueOf(property.getValue()))));
                }
            }

            case "remove" -> {
                if (args.length < 5) {
                    player.sendMessage(messages.get("service.property.remove.usage"));
                    return;
                }

                final String key = args[4];
                final Property<?> property = service.getProperty(key);

                if (property == null) {
                    player.sendMessage(messages.get("service.property.not-found")
                            .replaceText(text -> text.match("%key%").replacement(key)));
                    return;
                }

                service.getPropertyMap().remove(property.getName());
                service.update();

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
                    final Property<?> property = PropertyUtil.stringToProperty(key, value);
                    service.setProperty(property);
                    service.update();

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
        final Service service = serviceManager.getService(name);
        if (service == null) {
            player.sendMessage(messages.get("no-service"));
            return;
        }

        final String template = args[3];

        String filter;
        if (args.length >= 5) {
            filter = args[4];
        } else {
            filter = "";
        }


        if (filter.isEmpty()) {
            service.copy(template);
        } else {
            service.copy(template, filter);
        }

        player.sendMessage(messages.get("service.copy.success")
                .replaceText(text -> text.match("%files%").replacement(filter.isEmpty() ? "all service files" : filter))
                .replaceText(text -> text.match("%template%").replacement(template)));
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
            return serviceManager.getAllServices().stream()
                    .map(Service::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
        }


        if (sub.equalsIgnoreCase("copy") && args.length == 4) {
            final Service service = serviceManager.getService(args[2]);
            if (service == null) {
                return List.of();
            }
            return service.getServiceGroup().getServiceTemplates().stream().toList();
        }

        if (sub.equalsIgnoreCase("start") && args.length == 3) {
            return CloudAPI.getInstance().getServiceGroupManager().getAllServiceGroups().stream().map(ServiceGroup::getName).toList();
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
                return serviceManager.getAllServices().stream()
                        .map(Service::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                        .toList();
            }

            if (args.length == 5 && args[2].equalsIgnoreCase("remove")) {
                Service service = serviceManager.getService(args[3]);
                if (service != null) {
                    return service.getProperties().stream()
                            .map(Property::getName)
                            .filter(p -> p.toLowerCase().startsWith(args[4].toLowerCase()))
                            .toList();
                }
            }

            if (args.length == 5 && args[2].equalsIgnoreCase("set")) {
                List<String> completions = new ArrayList<>();
                completions.add("<custom>");
                completions.addAll(DefaultProperties.asSet().stream()
                        .map(Property::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[4].toLowerCase()))
                        .toList());
                return completions;
            }
        }

        return List.of();
    }
}
