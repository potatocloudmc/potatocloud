package net.potatocloud.plugins.optional.npc.util;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.api.service.Service;

import java.util.List;

public final class Placeholders {

    private static final PropertyKey<String> MOTD = PropertyKey.of("motd", "");

    private Placeholders() {
    }

    public static String service(String text, Service service) {
        return text.replace("%name%", service.name())
                .replace("%group%", service.group().name())
                .replace("%state%", service.get(DefaultProperties.GAME_STATE))
                .replace("%online_players%", String.valueOf(service.playerCount()))
                .replace("%max_players%", String.valueOf(service.maxPlayers()))
                .replace("%motd%", service.get(MOTD));
    }

    public static String group(String text, String groupName) {
        final List<Service> services = runningServices(groupName);
        return text.replace("%group%", groupName)
                .replace("%online_players%", String.valueOf(services.stream().mapToInt(Service::playerCount).sum()))
                .replace("%max_players%", String.valueOf(services.stream().mapToInt(Service::maxPlayers).sum()))
                .replace("%services%", String.valueOf(services.size()));
    }

    public static List<Service> runningServices(String groupName) {
        return CloudAPI.instance().groupManager().find(groupName)
                .map(Group::services)
                .orElse(List.of())
                .stream()
                .filter(Service::running)
                .toList();
    }
}
