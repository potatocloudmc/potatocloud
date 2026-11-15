package net.potatocloud.plugins.optional.sign;

import net.potatocloud.common.config.Config;
import net.potatocloud.common.config.yaml.YamlConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SignConfig {

    private final Config config;
    private final Map<String, List<List<String>>> layouts = new HashMap<>();
    private String permission;
    private int updateInterval;
    private List<String> hiddenStates;
    private boolean knockbackEnabled;
    private double knockbackDistance;
    private double knockbackStrength;
    private boolean knockbackSoundEnabled;
    private String knockbackSound;

    public SignConfig(Path path) {
        this.config = new YamlConfig(path);
    }

    public void load() {
        config.load();

        permission = config.get("permission").asString();
        updateInterval = Math.max(1, config.get("update-interval").asInt());
        hiddenStates = config.get("hidden-states").asStringList();
        knockbackEnabled = config.get("knockback.enabled").asBoolean();
        knockbackDistance = config.get("knockback.distance").asDouble();
        knockbackStrength = config.get("knockback.strength").asDouble();
        knockbackSoundEnabled = config.get("knockback.sound.enabled").asBoolean();
        knockbackSound = config.get("knockback.sound.name").asString();

        layouts.clear();
        for (String layout : config.get("layouts").keys()) {
            final List<List<String>> frames = new ArrayList<>();
            for (String frame : config.get("layouts." + layout).keys()) {
                frames.add(config.get("layouts." + layout + "." + frame).asStringList());
            }
            layouts.put(layout, frames);
        }
    }

    public String permission() {
        return permission;
    }

    public int updateInterval() {
        return updateInterval;
    }

    public List<String> hiddenStates() {
        return hiddenStates;
    }

    public boolean knockbackEnabled() {
        return knockbackEnabled;
    }

    public double knockbackDistance() {
        return knockbackDistance;
    }

    public double knockbackStrength() {
        return knockbackStrength;
    }

    public boolean knockbackSoundEnabled() {
        return knockbackSoundEnabled;
    }

    public String knockbackSound() {
        return knockbackSound;
    }

    public List<List<String>> layout(String name) {
        return layouts.getOrDefault(name, List.of());
    }
}
