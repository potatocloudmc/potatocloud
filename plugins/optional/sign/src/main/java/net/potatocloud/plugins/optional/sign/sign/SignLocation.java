package net.potatocloud.plugins.optional.sign.sign;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Optional;

public record SignLocation(String world, int x, int y, int z) {

    public static SignLocation of(Block block) {
        return new SignLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public Optional<Block> block() {
        final World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null || !bukkitWorld.isChunkLoaded(x >> 4, z >> 4)) {
            return Optional.empty();
        }
        return Optional.of(bukkitWorld.getBlockAt(x, y, z));
    }
}
