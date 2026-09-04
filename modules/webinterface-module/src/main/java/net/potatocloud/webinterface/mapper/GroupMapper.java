package net.potatocloud.webinterface.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.Node;
import net.potatocloud.webinterface.dto.request.GroupCreateRequest;
import net.potatocloud.webinterface.dto.response.GroupSummaryResponse;
import net.potatocloud.webinterface.model.ApiGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class GroupMapper {

    @Inject
    PlatformMapper platformMapper;

    @Inject
    PropertyMapper propertyMapper;

    public ApiGroup toApi(Group group) {
        Map<PropertyKey<?>, Object> properties = group.properties();

        return new ApiGroup()
                .name(group.name())
                .javaCommand(group.javaCommand())
                .platform(platformMapper.toApi(group.platform()))
                .platformVersion(platformMapper.toApi(group.platformVersion()))
                .staticServices(group.staticServices())
                .fallback(group.fallback())
                .onlineServicesCount(group.services().size())
                .onlinePlayerCount(group.platform().proxy() ? group.services().stream().mapToInt(Service::playerCount).sum() : group.players().size())
                .minServices(group.minServices())
                .maxServices(group.maxServices())
                .maxPlayers(group.maxPlayers())
                .maxMemory(group.maxMemory())
                .startPriority(group.startPriority())
                .startPercentage(group.startPercentage())
                .customJvmFlags(group.customJvmFlags())
                .templates(group.templates())
                .properties(propertyMapper.toApiList(properties))
                .useModernVelocityForwarding(properties.entrySet().stream()
                        .anyMatch(entry -> "velocityModernForwarding".equals(entry.getKey().name()) && Boolean.TRUE.equals(entry.getValue()))
                );
    }

    public Group toGroup(GroupCreateRequest request, String javaCommand, Set<String> customJvmFlags, Map<PropertyKey<?>, Object> propertyMap) {
        return CloudAPI.instance().groupManager().builder(request.name())
                .node(Node.instance().config().cluster().name())
                .platform(request.platform())
                .platformVersion(request.platformVersion())
                .minServices(request.minServices())
                .maxServices(request.maxServices())
                .maxPlayers(request.maxPlayerCount())
                .maxMemory(request.maxMemory())
                .fallback(request.fallback())
                .staticServices(request.staticServices())
                .startPriority(request.startPriority())
                .startPercentage(request.startPercentage())
                .javaCommand(javaCommand)
                .properties(propertyMap)
                .customJvmFlags(customJvmFlags)
                .build();
    }

    public GroupSummaryResponse toSummary(ApiGroup group) {
        return new GroupSummaryResponse()
                .name(group.name())
                .javaCommand(group.javaCommand())
                .platformName(group.platform().name())
                .platformVersion(group.platformVersion())
                .staticServices(group.staticServices())
                .fallback(group.fallback())
                .onlineServicesCount(group.onlineServicesCount())
                .onlinePlayerCount(group.onlinePlayerCount())
                .minServices(group.minServices())
                .maxServices(group.maxServices())
                .maxPlayers(group.maxPlayers())
                .maxMemory(group.maxMemory())
                .startPriority(group.startPriority())
                .startPercentage(group.startPercentage())
                .customJvmFlags(group.customJvmFlags())
                .templates(group.templates())
                .properties(group.properties())
                .useModernVelocityForwarding(group.useModernVelocityForwarding());
    }

    public List<GroupSummaryResponse> toSummary(List<ApiGroup> groups) {
        List<GroupSummaryResponse> summary = new ArrayList<>();
        for (ApiGroup group : groups) {
            summary.add(toSummary(group));
        }
        return summary;
    }
}
