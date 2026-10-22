package net.potatocloud.plugins.optional.npc.npc;

import net.potatocloud.common.config.Config;
import net.potatocloud.plugins.optional.npc.action.ActionType;
import net.potatocloud.plugins.optional.npc.action.NpcAction;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class NpcStorage {

    private final Config config;
    private final Logger logger;

    public NpcStorage(Config config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public List<Npc> load() {
        config.reload();

        final List<Npc> npcs = new ArrayList<>();
        for (String name : config.get("npcs").keys()) {
            try {
                npcs.add(load(name, "npcs." + name));
            } catch (IllegalArgumentException | NullPointerException e) {
                logger.warning("Skipping npc " + name + " because its config is invalid");
            }
        }
        return npcs;
    }

    public void save(Npc npc) {
        final String path = "npcs." + npc.name();
        final NpcLocation location = npc.location();

        config.remove(path);
        switch (npc) {
            case PlayerNpc playerNpc -> {
                config.set(path + ".type", "player");
                config.set(path + ".skin", playerNpc.skin());
            }
            case MobNpc mobNpc -> {
                config.set(path + ".type", "mob");
                config.set(path + ".entity", mobNpc.entityType().name());
            }
            default -> throw new IllegalStateException("Unexpected value: " + npc);
        }

        config.set(path + ".group", npc.group());
        config.set(path + ".look-at-player", npc.lookAtPlayer());
        config.set(path + ".hologram", List.copyOf(npc.hologram()));
        saveAction(path + ".left-click", npc.leftClick());
        saveAction(path + ".right-click", npc.rightClick());
        config.set(path + ".location.world", location.world());
        config.set(path + ".location.x", location.x());
        config.set(path + ".location.y", location.y());
        config.set(path + ".location.z", location.z());
        config.set(path + ".location.yaw", location.yaw());
        config.set(path + ".location.pitch", location.pitch());
        config.save();
    }

    public void delete(Npc npc) {
        config.remove("npcs." + npc.name());
        config.save();
    }

    private Npc load(String name, String path) {
        final String type = config.get(path + ".type").asString();
        final Npc npc = switch (type) {
            case "player" -> new PlayerNpc(name, Objects.requireNonNull(config.get(path + ".skin").asString()));
            case "mob" -> new MobNpc(name, EntityType.valueOf(config.get(path + ".entity").asString()));
            default -> throw new IllegalArgumentException("Unknown npc type " + type);
        };

        npc.group(config.get(path + ".group").asString());
        npc.lookAtPlayer(config.get(path + ".look-at-player").asBoolean());
        npc.hologram(config.get(path + ".hologram").asStringList());
        npc.leftClick(loadAction(path + ".left-click"));
        npc.rightClick(loadAction(path + ".right-click"));
        npc.location(new NpcLocation(
                config.get(path + ".location.world").asString(),
                config.get(path + ".location.x").asDouble(),
                config.get(path + ".location.y").asDouble(),
                config.get(path + ".location.z").asDouble(),
                (float) config.get(path + ".location.yaw").asDouble(),
                (float) config.get(path + ".location.pitch").asDouble()));
        return npc;
    }

    private void saveAction(String path, NpcAction action) {
        config.set(path + ".action", action.type().name().toLowerCase());
        if (action.text() != null) {
            config.set(path + ".text", action.text());
        }
    }

    private NpcAction loadAction(String path) {
        final ActionType type = ActionType.valueOf(config.get(path + ".action").asString().toUpperCase());
        return new NpcAction(type, config.get(path + ".text").asString());
    }
}
