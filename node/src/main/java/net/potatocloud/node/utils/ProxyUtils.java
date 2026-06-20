package net.potatocloud.node.utils;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.api.property.Property;
import net.potatocloud.node.Node;

import java.util.List;

public final class ProxyUtils {

    private ProxyUtils() {
    }

    public static Group getProxyGroup() {
        return getProxyGroups().stream().findFirst().orElse(null);
    }

    public static List<Group> getProxyGroups() {
        return Node.instance().groupManager().groups().stream().filter(group -> group.platform().proxy()).toList();
    }

    public static boolean isProxyModernForwarding() {
        if (getProxyGroup() == null) {
            return false;
        }

        final Property<Boolean> property = getProxyGroup().property(DefaultProperties.VELOCITY_MODERN_FORWARDING);
        if (property == null) {
            return false;
        }

        return property.value();
    }
}
