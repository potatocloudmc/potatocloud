package net.potatocloud.api.group.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ServiceGroupImpl implements ServiceGroup {

    private final String name;
    private final String platformName;
    private final String platformVersionName;
    private final List<String> serviceTemplates;
    private int minOnlineCount;
    private int maxOnlineCount;
    private int maxPlayers;
    private int maxMemory;
    private boolean fallback;
    private boolean isStatic;
    private int startPriority;
    private int startPercentage;
    private String javaCommand;
    private List<String> customJvmFlags;
    private final Map<String, Property<?>> propertyMap;

    public ServiceGroupImpl(String name, String platformName, String platformVersionName, int minOnlineCount, int maxOnlineCount, int maxPlayers, int maxMemory, boolean fallback, boolean isStatic, int startPriority, int startPercentage, String javaCommand, List<String> customJvmFlags, Map<String, Property<?>> propertyMap) {
        this.name = name;
        this.platformName = platformName;
        this.platformVersionName = platformVersionName;
        this.minOnlineCount = minOnlineCount;
        this.maxOnlineCount = maxOnlineCount;
        this.maxPlayers = maxPlayers;
        this.maxMemory = maxMemory;
        this.fallback = fallback;
        this.isStatic = isStatic;
        this.startPriority = startPriority;
        this.startPercentage = startPercentage;
        this.javaCommand = javaCommand;
        this.customJvmFlags = customJvmFlags;
        this.propertyMap = propertyMap;

        this.serviceTemplates = new ArrayList<>();
        addServiceTemplate("every");
        addServiceTemplate(name);

        final Platform platform = getPlatform();
        if (platform == null) {
            return;
        }

        if (platform.isProxy()) {
            addServiceTemplate("every_proxy");
        } else {
            addServiceTemplate("every_service");
        }
    }

    @Override
    public String getPropertyHolderName() {
        return getName();
    }

    @Override
    public <T> void setProperty(Property<T> property, T value) {
        ServiceGroup.super.setProperty(property, value);

        final Property<T> prop = getProperty(property.getName());
        if (prop != null) {
            for (Service service : getAllServices()) {
                service.setProperty(prop, prop.getValue(), false);
                service.update();
            }
        }
    }

    @Override
    public void addCustomJvmFlag(String flag) {
        customJvmFlags.add(flag);
    }

    @Override
    public void addServiceTemplate(String template) {
        if (serviceTemplates.contains(template)) {
            return;
        }
        serviceTemplates.add(template);
    }

    @Override
    public void removeServiceTemplate(String template) {
        if (!serviceTemplates.contains(template)) {
            return;
        }
        serviceTemplates.remove(template);
    }
}
