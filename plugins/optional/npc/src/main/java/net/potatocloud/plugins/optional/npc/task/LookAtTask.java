package net.potatocloud.plugins.optional.npc.task;

import io.papermc.paper.entity.LookAnchor;
import net.potatocloud.common.config.Config;
import net.potatocloud.plugins.optional.npc.entity.NpcEntityService;
import net.potatocloud.plugins.optional.npc.entity.SpawnedNpc;
import net.potatocloud.plugins.optional.npc.npc.NpcLocation;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Comparator;

public final class LookAtTask implements Runnable {

    private final NpcEntityService entityService;
    private final Config config;

    public LookAtTask(NpcEntityService entityService, Config config) {
        this.entityService = entityService;
        this.config = config;
    }

    @Override
    public void run() {
        final double range = config.get("look-at-range").asDouble();

        for (SpawnedNpc spawnedNpc : entityService.spawned()) {
            if (!spawnedNpc.npc().lookAtPlayer()) {
                continue;
            }

            final Entity entity = spawnedNpc.entity();
            final Location location = entity.getLocation();
            final NpcLocation npcLocation = spawnedNpc.npc().location();

            location.getNearbyPlayers(range).stream()
                    .min(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(location)))
                    .ifPresentOrElse(player -> entity.lookAt(player.getEyeLocation(), LookAnchor.EYES), () -> entity.setRotation(npcLocation.yaw(), npcLocation.pitch()));
        }
    }
}
