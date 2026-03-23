package net.potatocloud.plugin.server.signs.sign;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

@Getter
@Accessors(fluent = true)
public class SignLocation {

    private final String world;
    private final Double x, y, z;

    public SignLocation(String world, Double x, Double y, Double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SignLocation(Location location) {
        if (location.getWorld() != null) {
            this.world = location.getWorld().getName();
        }else {
            this.world = "world";
        }
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z);
    }

    public Block block() {
        return this.toLocation().getBlock();
    }

    @Override
    public String toString() {
        return world + " (" + x + ", " + y + ", " + z + ")";
    }
}
