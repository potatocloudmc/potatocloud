package net.potatocloud.node.platform;

import java.util.UUID;

public class VelocityForwardingSecret {

    public static final String FORWARDING_SECRET;

    static {
        FORWARDING_SECRET = UUID.randomUUID().toString().replace("-", "");
    }
}
