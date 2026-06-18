package net.potatocloud.webinterface.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.webinterface.dto.request.GroupCreateRequest;
import net.potatocloud.webinterface.dto.request.GroupUpdateRequest;
import net.potatocloud.webinterface.mapper.GroupMapper;
import net.potatocloud.webinterface.mapper.PropertyMapper;
import net.potatocloud.webinterface.model.ApiGroup;
import net.potatocloud.webinterface.model.ApiProperty;
import net.potatocloud.webinterface.service.GroupService;

import java.util.*;

@ApplicationScoped
public class GroupServiceImpl implements GroupService {

    @Inject
    GroupMapper groupMapper;

    @Inject
    PropertyMapper propertyMapper;

    private static <T> void changeIfDifferent(T newValue, T currentValue, java.util.function.Consumer<T> setter) {
        if (newValue != null && !newValue.equals(currentValue)) {
            setter.accept(newValue);
        }
    }

    private static void changeIfDifferentInt(Integer newValue, int currentValue, java.util.function.IntConsumer setter) {
        if (newValue != null && newValue != currentValue) {
            setter.accept(newValue);
        }
    }

    @Override
    public List<ApiGroup> findAll() {
        List<Group> groups = CloudAPI.instance().groupManager().groups();

        List<ApiGroup> apiGroups = new ArrayList<>();

        for (Group group : groups) {
            ApiGroup apiGroup = groupMapper.toApi(group);
            apiGroups.add(apiGroup);
        }

        return apiGroups;
    }

    @Override
    public boolean create(GroupCreateRequest request) {
        CloudAPI cloudAPI = CloudAPI.instance();

        if (cloudAPI.groupManager().exists(request.name())) {
            return false;
        }

        HashMap<String, Property<?>> generatedProperties = new HashMap<>();
        if (request.useModernVelocityForwarding()) {
            generatedProperties.put("velocityModernForwarding", new Property<>("velocityModernForwarding", false, true));
        }

        String startCommand = defaultIfBlank(request.javaCommand(), "java");

        Set<String> customJvmFlags = new HashSet<>();
        if (request.customJvmFlags() != null) {
            customJvmFlags.addAll(request.customJvmFlags());
        }

        Map<String, Property<?>> customProperties = new HashMap<>(generatedProperties);
        if (request.properties() != null) {
            for (ApiProperty apiProperty : request.properties()) {
                customProperties.put(
                        apiProperty.name(),
                        propertyMapper.toProperty(apiProperty)
                );
            }
        }

        cloudAPI.groupManager().create(groupMapper.toGroup(request, startCommand, customJvmFlags, customProperties));
        return true;
    }

    @Override
    public boolean update(String groupName, GroupUpdateRequest request) {
        CloudAPI cloudAPI = CloudAPI.instance();

        Group group = cloudAPI.groupManager().find(groupName).orElse(null);

        if (group == null) {
            return false;
        }

        changeIfDifferentInt(request.maxPlayers(), group.maxPlayers(), group::maxPlayers);
        changeIfDifferentInt(request.maxMemory(), group.maxMemory(), group::maxMemory);
        changeIfDifferentInt(request.minServices(), group.minServices(), group::minServices);
        changeIfDifferentInt(request.maxServices(), group.maxServices(), group::maxServices);
        changeIfDifferent(request.fallback(), group.fallback(), group::fallback);
        changeIfDifferent(request.startPriority(), group.startPriority(), group::startPriority);
        changeIfDifferent(request.startPercentage(), group.startPercentage(), group::startPercentage);

        if (request.templates() != null) {
            Set<String> currentTemplates = new HashSet<>(group.templates());
            Set<String> newTemplates = new HashSet<>(request.templates());

            if (!currentTemplates.equals(newTemplates)) {
                group.templates().clear();
                group.templates().addAll(newTemplates);
            }
        }

        if (request.customJvmFlags() != null) {
            Set<String> currentFlags = new HashSet<>(group.customJvmFlags());
            Set<String> newFlags = new HashSet<>(request.customJvmFlags());

            if (!currentFlags.equals(newFlags)) {
                group.customJvmFlags().clear();
                group.customJvmFlags().addAll(newFlags);
            }
        }

        if (request.properties() != null) {
            group.propertyMap().clear();
            for (ApiProperty apiProperty : request.properties()) {
                Property<?> newProperty = propertyMapper.toProperty(apiProperty);
                Property<?> currentProperty = group.propertyMap().get(apiProperty.name());

                if (currentProperty == null || !currentProperty.equals(newProperty)) {
                    group.propertyMap().put(apiProperty.name(), newProperty);
                }
            }
        }

        cloudAPI.groupManager().update(group);
        return true;
    }

    @Override
    public boolean exists(String name) {
        return CloudAPI.instance().groupManager().exists(name);
    }

    @Override
    public ApiGroup findByName(String name) {
        Group group = CloudAPI.instance().groupManager().find(name).orElse(null);

        if (group == null) {
            return null;
        }

        return groupMapper.toApi(group);
    }

    @Override
    public boolean start(String name) {
        Optional<Group> optionalGroup = CloudAPI.instance().groupManager().find(name);
        if (optionalGroup.isEmpty()) {
            return false;
        }

        Group group = optionalGroup.get();

        CloudAPI.instance().serviceManager().start(group);
        return true;
    }

    @Override
    public boolean canStartService(String name) {
        Group group = CloudAPI.instance().groupManager().find(name).orElse(null);

        if (group == null) {
            return false;
        }

        int services = group.services().size();
        int maxServices = group.maxServices();

        return services < maxServices;
    }

    @Override
    public boolean shutdown(String name) {
        Group group = CloudAPI.instance().groupManager().find(name).orElse(null);

        if (group == null) {
            return false;
        }

        for (Service service : group.services()) {
            CloudAPI.instance().serviceManager().stop(service);
        }

        return true;
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
