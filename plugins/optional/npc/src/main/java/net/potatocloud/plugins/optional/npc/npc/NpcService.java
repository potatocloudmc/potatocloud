package net.potatocloud.plugins.optional.npc.npc;

import net.potatocloud.plugins.optional.npc.entity.NpcEntityService;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class NpcService {

    private final Map<String, Npc> npcs = new LinkedHashMap<>();
    private final NpcStorage storage;
    private final NpcEntityService entityService;

    public NpcService(NpcStorage storage, NpcEntityService entityService) {
        this.storage = storage;
        this.entityService = entityService;
    }

    public void load() {
        entityService.despawnAll();
        npcs.clear();

        for (Npc npc : storage.load()) {
            npcs.put(npc.name().toLowerCase(), npc);
            entityService.spawn(npc);
        }
    }

    public void create(Npc npc) {
        npcs.put(npc.name().toLowerCase(), npc);
        storage.save(npc);
        entityService.spawn(npc);
    }

    public void update(Npc npc) {
        storage.save(npc);
        entityService.despawn(npc);
        entityService.spawn(npc);
    }

    public void delete(Npc npc) {
        npcs.remove(npc.name().toLowerCase());
        storage.delete(npc);
        entityService.despawn(npc);
    }

    public Optional<Npc> find(String name) {
        return Optional.ofNullable(npcs.get(name.toLowerCase()));
    }

    public Collection<Npc> npcs() {
        return npcs.values();
    }
}
