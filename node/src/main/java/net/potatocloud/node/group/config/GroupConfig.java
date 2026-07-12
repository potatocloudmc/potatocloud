package net.potatocloud.node.group.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.impl.GroupImpl;
import net.potatocloud.api.property.PropertyKey;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record GroupConfig(
        String name,
        String node,
        String platform,
        @JsonProperty("platform-version") String platformVersion,
        @JsonProperty("java-command") String javaCommand,
        @JsonProperty("jvm-flags") Set<String> jvmFlags,
        @JsonProperty("max-players") int maxPlayers,
        @JsonProperty("max-memory") int maxMemory,
        @JsonProperty("min-online-count") int minOnlineCount,
        @JsonProperty("max-online-count") int maxOnlineCount,
        @JsonProperty("static") boolean isStatic,
        boolean fallback,
        @JsonProperty("start-priority") int startPriority,
        @JsonProperty("start-percentage") int startPercentage,
        Set<String> templates,
        List<PropertyConfig> properties
) {

    public static GroupConfig from(Group group) {
        return new GroupConfig(
                group.name(),
                group.node().map(ClusterNode::name).orElse(null),
                group.platform().name(),
                group.platformVersion().name(),
                group.javaCommand(),
                group.customJvmFlags(),
                group.maxPlayers(),
                group.maxMemory(),
                group.minServices(),
                group.maxServices(),
                group.staticServices(),
                group.fallback(),
                group.startPriority(),
                group.startPercentage(),
                group.templates(),
                group.properties().entrySet().stream().map(PropertyConfig::from).toList()
        );
    }


    public Group toGroup() {
        final Map<PropertyKey<?>, Object> propertyMap = new HashMap<>();

        if (properties != null) {
            for (PropertyConfig property : properties) {
                final Map.Entry<PropertyKey<?>, Object> entry = property.toEntry();

                if (propertyMap.containsKey(entry.getKey())) {
                    throw new IllegalStateException("Duplicate property " + entry.getKey().name() + " found in group " + this.name);
                }

                propertyMap.put(entry.getKey(), entry.getValue());
            }
        }

        return new GroupImpl(
                name,
                node,
                platform,
                platformVersion,
                javaCommand,
                jvmFlags != null ? jvmFlags : new HashSet<>(),
                maxPlayers,
                maxMemory,
                minOnlineCount,
                maxOnlineCount,
                isStatic,
                fallback,
                startPriority,
                startPercentage,
                templates != null ? templates : new HashSet<>(),
                propertyMap
        );
    }
}