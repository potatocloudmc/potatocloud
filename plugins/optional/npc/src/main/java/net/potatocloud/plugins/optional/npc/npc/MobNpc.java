package net.potatocloud.plugins.optional.npc.npc;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public final class MobNpc extends Npc {

    private EntityType entityType;

    public MobNpc(String name, EntityType entityType) {
        super(name);
        this.entityType = entityType;
    }

    @Override
    public Entity spawn(Location location) {
        return location.getWorld().spawnEntity(location, entityType);
    }

    public EntityType entityType() {
        return entityType;
    }

    public void entityType(EntityType entityType) {
        this.entityType = entityType;
    }
}
