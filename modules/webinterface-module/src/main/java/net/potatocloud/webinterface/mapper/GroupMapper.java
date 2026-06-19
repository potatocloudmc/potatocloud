package net.potatocloud.webinterface.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.property.Property;
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
                .properties(group.properties().stream().map(propertyMapper::toApi).toList())
                .useModernVelocityForwarding(group.properties().stream().anyMatch(property -> "velocityModernForwarding".equals(property.name()) && Boolean.TRUE.equals(property.value())));
    }

    public Group toGroup(GroupCreateRequest request, String javaCommand, Set<String> customJvmFlags, Map<String, Property<?>> propertyMap) {
        return CloudAPI.instance().groupManager().builder(request.name())
                .node(Node.getInstance().config().cluster().name())
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
