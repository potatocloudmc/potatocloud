package net.potatocloud.plugins.optional.sign.sign;

import net.potatocloud.common.config.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public final class SignStorage {

    private final Config config;
    private final Logger logger;

    public SignStorage(Config config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public List<CloudSign> load() {
        config.reload();

        final List<CloudSign> signs = new ArrayList<>();
        for (String entry : config.get("signs").asStringList()) {
            try {
                final String[] parts = entry.split(" ");
                final SignLocation location = new SignLocation(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
                signs.add(new CloudSign(parts[0], location));
            } catch (Exception e) {
                logger.warning("Skipping sign " + entry + " because its config is invalid");
            }
        }
        return signs;
    }

    public void save(Collection<CloudSign> signs) {
        final List<String> list = new ArrayList<>();
        for (CloudSign sign : signs) {
            final SignLocation location = sign.location();
            list.add(sign.group() + " " + location.world() + " " + location.x() + " " + location.y() + " " + location.z());
        }

        config.set("signs", list);
        config.save();
    }
}
