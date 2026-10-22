package net.potatocloud.plugins.optional.npc.entity;

import net.potatocloud.plugins.optional.npc.npc.Npc;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

public record SpawnedNpc(Npc npc, Entity entity, TextDisplay hologram) {

    public void remove() {
        entity.remove();
        if (hologram != null) {
            hologram.remove();
        }
    }
}
