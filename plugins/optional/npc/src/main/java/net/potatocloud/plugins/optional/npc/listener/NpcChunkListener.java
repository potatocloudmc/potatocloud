package net.potatocloud.plugins.optional.npc.listener;

import net.potatocloud.plugins.optional.npc.entity.NpcEntityService;
import net.potatocloud.plugins.optional.npc.npc.Npc;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.plugin.Plugin;

public final class NpcChunkListener implements Listener {

    private final Plugin plugin;
    private final NpcService npcService;
    private final NpcEntityService entityService;

    public NpcChunkListener(Plugin plugin, NpcService npcService, NpcEntityService entityService) {
        this.plugin = plugin;
        this.npcService = npcService;
        this.entityService = entityService;
    }

    @EventHandler
    public void onLoad(EntitiesLoadEvent event) {
        final Chunk chunk = event.getChunk();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (Npc npc : npcService.npcs()) {
                if (npc.location().isIn(chunk)) {
                    entityService.spawn(npc);
                }
            }
        });
    }

    @EventHandler
    public void onUnload(EntitiesUnloadEvent event) {
        entityService.despawnIn(event.getChunk());
    }
}
