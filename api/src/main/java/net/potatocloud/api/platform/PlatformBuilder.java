package net.potatocloud.api.platform;

import net.potatocloud.api.platform.impl.PlatformImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link Platform}.
 */
public class PlatformBuilder {

    private final String name;
    private String downloadUrl = null;
    private boolean custom = false;
    private boolean proxy = false;
    private PlatformBase base = PlatformBase.UNKNOWN;
    private String preCacheBuilder = null;
    private String parser = null;
    private String hashType = null;
    private List<String> prepareSteps = new ArrayList<>();

    /**
     * Creates a builder for a platform.
     *
     * @param name the platform name
     */
    public PlatformBuilder(String name) {
        this.name = name;
    }

    /**
     * Sets the download URL.
     *
     * @param downloadUrl the download URL
     * @return this builder
     */
    public PlatformBuilder downloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    /**
     * Sets whether the platform is custom.
     *
     * @param custom whether the platform is custom
     * @return this builder
     */
    public PlatformBuilder custom(boolean custom) {
        this.custom = custom;
        return this;
    }

    /**
     * Sets whether the platform is a proxy.
     *
     * @param proxy whether the platform is a proxy
     * @return this builder
     */
    public PlatformBuilder proxy(boolean proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Sets the platform base.
     *
     * @param base the platform base
     * @return this builder
     */
    public PlatformBuilder base(PlatformBase base) {
        this.base = base;
        return this;
    }

    /**
     * Sets the pre-cache builder name.
     *
     * @param preCacheBuilder the pre-cache builder name
     * @return this builder
     */
    public PlatformBuilder preCacheBuilder(String preCacheBuilder) {
        this.preCacheBuilder = preCacheBuilder;
        return this;
    }

    /**
     * Sets the parser name.
     *
     * @param parser the parser name
     * @return this builder
     */
    public PlatformBuilder parser(String parser) {
        this.parser = parser;
        return this;
    }

    /**
     * Sets the file hash type.
     *
     * @param hashType the hash type
     * @return this builder
     */
    public PlatformBuilder hashType(String hashType) {
        this.hashType = hashType;
        return this;
    }

    /**
     * Adds one prepare step.
     *
     * @param step the prepare step
     * @return this builder
     */
    public PlatformBuilder prepareStep(String step) {
        this.prepareSteps.add(step);
        return this;
    }

    /**
     * Adds several prepare steps.
     *
     * @param prepareSteps the prepare steps
     * @return this builder
     */
    public PlatformBuilder prepareSteps(List<String> prepareSteps) {
        this.prepareSteps = new ArrayList<>(prepareSteps);
        return this;
    }

    /**
     * Builds the configured platform.
     *
     * @return the created platform
     */
    public Platform build() {
        return new PlatformImpl(name, downloadUrl, custom, proxy, base, preCacheBuilder, parser, hashType, prepareSteps);
    }
}
