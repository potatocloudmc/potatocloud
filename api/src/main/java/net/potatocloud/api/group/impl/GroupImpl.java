package net.potatocloud.api.group.impl;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;

import java.util.*;

public class GroupImpl implements Group {

    private final String name;
    private final String nodeName;
    private final String platformName;
    private final String platformVersionName;
    private final String javaCommand;
    private final Set<String> customJvmFlags;
    private int maxPlayers;
    private int maxMemory;
    private int minServices;
    private int maxServices;
    private final boolean staticServices;
    private boolean fallback;
    private int startPriority;
    private int startPercentage;
    private final Set<String> templates;
    private final Map<String, Property<?>> propertyMap;

    public GroupImpl(
            String name,
            String nodeName,
            String platformName,
            String platformVersionName,
            String javaCommand,
            Set<String> customJvmFlags,
            int maxPlayers,
            int maxMemory,
            int minServices,
            int maxServices,
            boolean staticServices,
            boolean fallback,
            int startPriority,
            int startPercentage,
            Map<String, Property<?>> propertyMap
    ) {
        this(name, nodeName, platformName, platformVersionName, javaCommand, customJvmFlags, maxPlayers, maxMemory,
                minServices, maxServices, staticServices, fallback, startPriority, startPercentage, new HashSet<>(), propertyMap);

        addTemplate("every");
        addTemplate(name);

        final Platform platform = platform();
        if (platform != null) {
            addTemplate(platform.proxy() ? "every_proxy" : "every_service");
        }
    }

    public GroupImpl(
            String name,
            String nodeName,
            String platformName,
            String platformVersionName,
            String javaCommand,
            Set<String> customJvmFlags,
            int maxPlayers,
            int maxMemory,
            int minServices,
            int maxServices,
            boolean staticServices,
            boolean fallback,
            int startPriority,
            int startPercentage,
            Set<String> templates,
            Map<String, Property<?>> propertyMap
    ) {
        this.name = name;
        this.nodeName = nodeName;
        this.platformName = platformName;
        this.platformVersionName = platformVersionName;
        this.javaCommand = javaCommand;
        this.customJvmFlags = customJvmFlags;
        this.maxPlayers = maxPlayers;
        this.maxMemory = maxMemory;
        this.minServices = minServices;
        this.maxServices = maxServices;
        this.staticServices = staticServices;
        this.fallback = fallback;
        this.startPriority = startPriority;
        this.startPercentage = startPercentage;
        this.templates = templates;
        this.propertyMap = propertyMap;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Optional<ClusterNode> node() {
        return CloudAPI.instance().clusterManager().find(nodeName);
    }

    @Override
    public Platform platform() {
        return CloudAPI.instance().platformManager().find(platformName).orElseThrow(() -> new IllegalStateException("Platform not found: " + platformName));
    }

    @Override
    public PlatformVersion platformVersion() {
        return platform().version(platformVersionName)
                .orElseThrow(() -> new IllegalStateException("Platform version not found: " + platformVersionName));
    }

    @Override
    public Set<String> templates() {
        return templates;
    }

    @Override
    public int minServices() {
        return minServices;
    }

    @Override
    public void minServices(int minServices) {
        this.minServices = minServices;
    }

    @Override
    public int maxServices() {
        return maxServices;
    }

    @Override
    public void maxServices(int maxServices) {
        this.maxServices = maxServices;
    }

    @Override
    public int maxPlayers() {
        return maxPlayers;
    }

    @Override
    public void maxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    @Override
    public int maxMemory() {
        return maxMemory;
    }

    @Override
    public void maxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    @Override
    public boolean fallback() {
        return fallback;
    }

    @Override
    public void fallback(boolean fallback) {
        this.fallback = fallback;
    }

    @Override
    public boolean staticServices() {
        return staticServices;
    }

    @Override
    public int startPriority() {
        return startPriority;
    }

    @Override
    public void startPriority(int startPriority) {
        this.startPriority = startPriority;
    }

    @Override
    public int startPercentage() {
        return startPercentage;
    }

    @Override
    public void startPercentage(int startPercentage) {
        this.startPercentage = startPercentage;
    }

    @Override
    public String javaCommand() {
        return javaCommand;
    }

    @Override
    public Set<String> customJvmFlags() {
        return customJvmFlags;
    }

    @Override
    public void addCustomJvmFlag(String flag) {
        customJvmFlags.add(flag);
    }

    @Override
    public void addTemplate(String template) {
        templates.add(template);
    }

    @Override
    public void removeTemplate(String template) {
        templates.remove(template);
    }

    @Override
    public <T> void set(Property<T> property, T value) {
        Group.super.set(property, value);

        final Property<T> prop = property(property.name());
        if (prop != null) {
            for (Service service : services()) {
                service.set(prop, prop.value(), false);

                CloudAPI.instance().serviceManager().update(service);
            }
        }
    }

    @Override
    public Map<String, Property<?>> propertyMap() {
        return propertyMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroupImpl group)) {
            return false;
        }
        return name.equals(group.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
