package net.potatocloud.plugins.optional.npc.command;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class Arguments {

    private Arguments() {
    }

    public static boolean isNpcName(String value) {
        return value.matches("[A-Za-z0-9_-]{1,32}");
    }

    public static boolean isSkinName(String value) {
        return value.matches("[A-Za-z0-9_]{1,16}");
    }

    public static Optional<EntityType> parseMob(String value) {
        try {
            return Optional.of(EntityType.valueOf(value.toUpperCase())).filter(Arguments::isMob);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static List<String> mobs() {
        return Arrays.stream(EntityType.values())
                .filter(Arguments::isMob)
                .map(type -> type.name().toLowerCase())
                .toList();
    }

    public static List<String> groups() {
        return CloudAPI.instance().groupManager().groups().stream().map(Group::name).toList();
    }

    private static boolean isMob(EntityType type) {
        return type.isAlive() && type.isSpawnable() && type != EntityType.PLAYER && type != EntityType.MANNEQUIN;
    }
}
