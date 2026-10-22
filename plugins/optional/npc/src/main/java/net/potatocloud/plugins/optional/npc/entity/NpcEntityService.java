package net.potatocloud.plugins.optional.npc.entity;

import net.kyori.adventure.text.Component;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.util.Placeholders;
import net.potatocloud.plugins.optional.npc.util.TextUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class NpcEntityService {

    private static final double HOLOGRAM_OFFSET = 0.25;

    private final Map<String, SpawnedNpc> spawned = new HashMap<>();
    private final NamespacedKey npcKey;

    public NpcEntityService(Plugin plugin) {
        this.npcKey = new NamespacedKey(plugin, "npc");
    }

    public void spawn(Npc npc) {
        if (spawned.containsKey(key(npc))) {
            return;
        }

        npc.location().toBukkit()
                .filter(Location::isChunkLoaded)
                .ifPresent(location -> spawn(npc, location));
    }

    private void spawn(Npc npc, Location location) {
        final Entity entity = npc.spawn(location);
        setup(entity, npc);

        final TextDisplay hologram = npc.hologram().isEmpty() ? null : spawnHologram(npc, location, entity.getHeight());
        spawned.put(key(npc), new SpawnedNpc(npc, entity, hologram));
    }

    public void despawn(Npc npc) {
        Optional.ofNullable(spawned.remove(key(npc))).ifPresent(SpawnedNpc::remove);
    }

    public void despawnIn(Chunk chunk) {
        for (SpawnedNpc spawnedNpc : List.copyOf(spawned.values())) {
            if (spawnedNpc.npc().location().isIn(chunk)) {
                despawn(spawnedNpc.npc());
            }
        }
    }

    public void despawnAll() {
        spawned.values().forEach(SpawnedNpc::remove);
        spawned.clear();
    }

    public void updateHolograms() {
        for (SpawnedNpc spawnedNpc : spawned.values()) {
            if (spawnedNpc.hologram() != null) {
                spawnedNpc.hologram().text(hologramText(spawnedNpc.npc()));
            }
        }
    }

    public Optional<Npc> find(Entity entity) {
        return Optional.ofNullable(entity.getPersistentDataContainer().get(npcKey, PersistentDataType.STRING))
                .map(spawned::get)
                .map(SpawnedNpc::npc);
    }

    public Collection<SpawnedNpc> spawned() {
        return spawned.values();
    }

    private void setup(Entity entity, Npc npc) {
        entity.setPersistent(false);
        entity.setSilent(true);
        entity.setInvulnerable(true);
        entity.setGravity(false);
        entity.setCustomNameVisible(false);
        entity.getPersistentDataContainer().set(npcKey, PersistentDataType.STRING, key(npc));

        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.setCollidable(false);
            livingEntity.setRemoveWhenFarAway(false);
        }

        if (entity instanceof Mob mob) {
            mob.setAI(false);
        }
    }

    private TextDisplay spawnHologram(Npc npc, Location npcLocation, double npcHeight) {
        final Location location = npcLocation.clone().add(0, npcHeight + HOLOGRAM_OFFSET, 0);
        return location.getWorld().spawn(location, TextDisplay.class, display -> {
            display.setPersistent(false);
            display.setBillboard(Display.Billboard.CENTER);
            display.setLineWidth(Integer.MAX_VALUE);
            display.text(hologramText(npc));
        });
    }

    private Component hologramText(Npc npc) {
        final String text = String.join("\n", npc.hologram()).replace("%name%", npc.name());
        return TextUtils.component(Placeholders.group(text, npc.group()));
    }

    private String key(Npc npc) {
        return npc.name().toLowerCase();
    }
}
