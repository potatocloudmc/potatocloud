package net.potatocloud.api.group;

import net.potatocloud.api.group.impl.GroupImpl;
import net.potatocloud.api.property.PropertyKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builds a {@link Group}.
 */
public final class GroupBuilder {

    private final String name;
    private final Set<String> customJvmFlags = new HashSet<>();
    private final Map<PropertyKey<?>, Object> properties = new HashMap<>();
    private String nodeName = "";
    private String platformName;
    private String platformVersionName;
    private int minServices = 1;
    private int maxServices = 1;
    private int maxPlayers = 100;
    private int maxMemory = 2048;
    private boolean fallback = false;
    private boolean staticServices = false;
    private int startPriority = 1;
    private int startPercentage = 80;
    private String javaCommand = "java";

    /**
     * Creates a builder for a group.
     *
     * @param name the group name
     */
    public GroupBuilder(String name) {
        this.name = name;
    }

    /**
     * Sets the target node name.
     *
     * @param nodeName the node name
     * @return this builder
     */
    public GroupBuilder node(String nodeName) {
        this.nodeName = nodeName;
        return this;
    }

    /**
     * Sets the platform name.
     *
     * @param platformName the platform name
     * @return this builder
     */
    public GroupBuilder platform(String platformName) {
        this.platformName = platformName;
        return this;
    }

    /**
     * Sets the platform version name.
     *
     * @param platformVersionName the platform version name
     * @return this builder
     */
    public GroupBuilder platformVersion(String platformVersionName) {
        this.platformVersionName = platformVersionName;
        return this;
    }

    /**
     * Sets the minimum service count.
     *
     * @param minServices the minimum service count
     * @return this builder
     */
    public GroupBuilder minServices(int minServices) {
        this.minServices = minServices;
        return this;
    }

    /**
     * Sets the maximum service count.
     *
     * @param maxServices the maximum service count
     * @return this builder
     */
    public GroupBuilder maxServices(int maxServices) {
        this.maxServices = maxServices;
        return this;
    }

    /**
     * Sets the maximum players per service.
     *
     * @param maxPlayers the maximum player count
     * @return this builder
     */
    public GroupBuilder maxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        return this;
    }

    /**
     * Sets the maximum memory per service in MB.
     *
     * @param maxMemory the maximum memory in MB
     * @return this builder
     */
    public GroupBuilder maxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
        return this;
    }

    /**
     * Sets whether this group is a fallback group.
     *
     * @param fallback whether the group is a fallback group
     * @return this builder
     */
    public GroupBuilder fallback(boolean fallback) {
        this.fallback = fallback;
        return this;
    }

    /**
     * Sets whether services in this group are static.
     *
     * @param staticServices whether services are static
     * @return this builder
     */
    public GroupBuilder staticServices(boolean staticServices) {
        this.staticServices = staticServices;
        return this;
    }

    /**
     * Sets the start priority.
     *
     * @param startPriority the start priority
     * @return this builder
     */
    public GroupBuilder startPriority(int startPriority) {
        this.startPriority = startPriority;
        return this;
    }

    /**
     * Sets the startup percentage.
     *
     * @param startPercentage the startup percentage
     * @return this builder
     */
    public GroupBuilder startPercentage(int startPercentage) {
        this.startPercentage = startPercentage;
        return this;
    }

    /**
     * Sets the Java command used to start services.
     *
     * @param javaCommand the Java command
     * @return this builder
     */
    public GroupBuilder javaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    /**
     * Adds one JVM flag.
     *
     * @param flag the JVM flag
     * @return this builder
     */
    public GroupBuilder customJvmFlag(String flag) {
        this.customJvmFlags.add(flag);
        return this;
    }

    /**
     * Adds one property.
     *
     * @param key the property key
     * @param value the property value
     * @param <T> the property type
     * @return this builder
     */
    public <T> GroupBuilder property(PropertyKey<T> key, T value) {
        this.properties.put(key, value);
        return this;
    }

    /**
     * Adds several properties.
     *
     * @param properties the properties to add
     * @return this builder
     */
    public GroupBuilder properties(Map<PropertyKey<?>, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    /**
     * Adds several JVM flags.
     *
     * @param customJvmFlags the JVM flags to add
     * @return this builder
     */
    public GroupBuilder customJvmFlags(Set<String> customJvmFlags) {
        this.customJvmFlags.addAll(customJvmFlags);
        return this;
    }

    /**
     * Builds the configured group.
     *
     * @return the created group
     */
    public Group build() {
        return new GroupImpl(
                name,
                nodeName,
                platformName,
                platformVersionName,
                javaCommand,
                customJvmFlags,
                maxPlayers,
                maxMemory,
                minServices,
                maxServices,
                staticServices,
                fallback,
                startPriority,
                startPercentage,
                properties
        );
    }
}
