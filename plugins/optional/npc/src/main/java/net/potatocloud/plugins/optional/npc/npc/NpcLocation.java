package net.potatocloud.plugins.optional.npc.npc;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Optional;

public record NpcLocation(String world, double x, double y, double z, float yaw, float pitch) {

    public static NpcLocation of(Location location) {
        return new NpcLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
    }

    public Optional<Location> toBukkit() {
        return Optional.ofNullable(Bukkit.getWorld(world)).map(bukkitWorld -> new Location(bukkitWorld, x, y, z, yaw, pitch));
    }

    public boolean isIn(Chunk chunk) {
        return chunk.getWorld().getName().equals(world) && chunk.getX() == chunkX() && chunk.getZ() == chunkZ();
    }

    private int chunkX() {
        return (int) Math.floor(x / 16.0);
    }

    private int chunkZ() {
        return (int) Math.floor(z / 16.0);
    }
}
