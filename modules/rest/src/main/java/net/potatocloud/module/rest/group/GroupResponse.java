package net.potatocloud.module.rest.group;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.template.Template;
import net.potatocloud.module.rest.property.PropertyResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record GroupResponse(
        String name,
        String node,
        String platform,
        String platformVersion,
        List<String> templates,
        int minServices,
        int maxServices,
        int maxPlayers,
        int maxMemory,
        boolean fallback,
        boolean maintenance,
        boolean staticServices,
        int startPriority,
        int startPercentage,
        String javaCommand,
        Set<String> customJvmFlags,
        Map<String, Object> properties,
        int onlineServices,
        int onlinePlayers
) {

    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.name(),
                group.node().map(ClusterNode::name).orElse(null),
                group.platform().name(),
                group.platformVersion().name(),
                group.templates().stream().map(Template::name).toList(),
                group.minServices(),
                group.maxServices(),
                group.maxPlayers(),
                group.maxMemory(),
                group.fallback(),
                group.maintenance(),
                group.staticServices(),
                group.startPriority(),
                group.startPercentage(),
                group.javaCommand(),
                group.customJvmFlags(),
                PropertyResponse.map(group),
                group.services().size(),
                group.players().size()
        );
    }
}
