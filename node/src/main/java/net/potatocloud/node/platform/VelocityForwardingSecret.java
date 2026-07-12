package net.potatocloud.node.platform;

import net.potatocloud.node.Node;
import net.potatocloud.node.config.ClusterConfig;
import net.potatocloud.node.utils.HashUtils;
import java.util.UUID;

public final class VelocityForwardingSecret {

    private VelocityForwardingSecret() {
    }

    public static final String FORWARDING_SECRET = createForwardingSecret();

    private static String createForwardingSecret() {
        final ClusterConfig clusterConfig = Node.instance().config().cluster();

        if (clusterConfig.enabled()) {
            return HashUtils.sha256(clusterConfig.token());
        }

        return UUID.randomUUID().toString().replace("-", "");
    }
}
