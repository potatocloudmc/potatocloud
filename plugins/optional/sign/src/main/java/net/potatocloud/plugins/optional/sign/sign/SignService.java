package net.potatocloud.plugins.optional.sign.sign;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.service.Service;
import net.potatocloud.plugins.optional.sign.SignConfig;
import net.potatocloud.plugins.shared.MessageUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SignService {

    private final Map<SignLocation, CloudSign> signs = new LinkedHashMap<>();
    private final Map<SignLocation, Service> services = new HashMap<>();
    private final SignStorage storage;
    private final SignConfig config;
    private int tick;

    public SignService(SignStorage storage, SignConfig config) {
        this.storage = storage;
        this.config = config;
    }

    public void load() {
        signs.clear();
        services.clear();

        for (CloudSign sign : storage.load()) {
            signs.put(sign.location(), sign);
        }
    }

    public void create(CloudSign sign) {
        signs.put(sign.location(), sign);
        storage.save(signs.values());
    }

    public void delete(CloudSign sign) {
        signs.remove(sign.location());
        services.remove(sign.location());
        storage.save(signs.values());
    }

    public Optional<CloudSign> find(Block block) {
        return Optional.ofNullable(signs.get(SignLocation.of(block)));
    }

    public Optional<Service> service(CloudSign sign) {
        return Optional.ofNullable(services.get(sign.location()));
    }

    public Collection<CloudSign> signs() {
        return signs.values();
    }

    public void update() {
        for (CloudSign sign : signs.values()) {
            final Service service = findService(sign);
            if (service == null) {
                services.remove(sign.location());
            } else {
                services.put(sign.location(), service);
            }
            render(sign, service);
            knockback(sign);
        }
        tick++;
    }

    private void knockback(CloudSign sign) {
        final Block block = sign.location().block().orElse(null);
        if (!config.knockbackEnabled() || block == null) {
            return;
        }

        // push players straight from the front of the sign
        final BlockFace facing = switch (block.getBlockData()) {
            case Directional directional -> directional.getFacing();
            case Rotatable rotatable -> rotatable.getRotation();
            default -> null;
        };

        if (facing == null) {
            return;
        }

        final Vector velocity = facing.getDirection().setY(0).normalize().multiply(config.knockbackStrength()).setY(0.2);
        final Location center = block.getLocation().toCenterLocation();
        for (Player player : center.getNearbyPlayers(config.knockbackDistance())) {
            if (player.hasPermission(config.permission())) {
                continue;
            }

            player.setVelocity(velocity);

            if (config.knockbackSoundEnabled()) {
                player.playSound(player.getLocation(), config.knockbackSound(), 1, 1);
            }
        }
    }

    private Service findService(CloudSign sign) {
        final List<Service> running = CloudAPI.instance().groupManager().find(sign.group())
                .map(Group::services)
                .orElse(List.of())
                .stream()
                .filter(Service::running)
                .filter(service -> !config.hiddenStates().contains(service.get(DefaultProperties.GAME_STATE)))
                .sorted(Comparator.comparingInt(Service::id))
                .toList();

        // keep the current service while it runs, so services do not jump between signs
        final Service current = services.get(sign.location());
        for (Service service : running) {
            if (service.equals(current)) {
                return service;
            }
        }

        return running.stream().filter(service -> !services.containsValue(service)).findFirst().orElse(null);
    }

    private void render(CloudSign sign, Service service) {
        final Block block = sign.location().block().orElse(null);
        if (block == null || !(block.getState() instanceof Sign state)) {
            return;
        }

        final String layout = service == null ? "searching" : service.full() ? "full" : "online";
        final List<List<String>> frames = config.layout(layout);
        final List<String> lines = frames.isEmpty() ? List.of() : frames.get(tick % frames.size());

        final SignSide side = state.getSide(Side.FRONT);
        for (int i = 0; i < 4; i++) {
            final String line = i < lines.size() ? replacePlaceholders(lines.get(i), sign, service) : "";
            side.line(i, MessageUtils.format(line));
        }

        state.setWaxed(true);
        state.update(false, false);
    }

    private String replacePlaceholders(String line, CloudSign sign, Service service) {
        final String text = line.replace("%group%", sign.group());
        if (service == null) {
            return text;
        }
        return text.replace("%name%", service.name())
                .replace("%state%", service.get(DefaultProperties.GAME_STATE))
                .replace("%online_players%", String.valueOf(service.playerCount()))
                .replace("%max_players%", String.valueOf(service.maxPlayers()));
    }
}
