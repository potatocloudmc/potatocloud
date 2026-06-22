package net.potatocloud.api.platform.impl;


import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformBase;
import net.potatocloud.api.platform.PlatformVersion;

import java.util.ArrayList;
import java.util.List;

public class PlatformImpl implements Platform {

    private final String name;
    private final String downloadUrl;
    private final boolean custom;
    private final boolean proxy;
    private final PlatformBase base;
    private final String preCacheBuilder;
    private final String parser;
    private final String hashType;
    private final List<String> prepareSteps;

    private List<PlatformVersion> versions = new ArrayList<>();

    public PlatformImpl(String name, String downloadUrl, boolean custom, boolean proxy, PlatformBase base, String preCacheBuilder, String parser, String hashType, List<String> prepareSteps) {
        this.name = name;
        this.downloadUrl = downloadUrl;
        this.custom = custom;
        this.proxy = proxy;
        this.base = base;
        this.preCacheBuilder = preCacheBuilder;
        this.parser = parser;
        this.hashType = hashType;
        this.prepareSteps = prepareSteps;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String downloadUrl() {
        return downloadUrl;
    }

    @Override
    public boolean custom() {
        return custom;
    }

    @Override
    public boolean proxy() {
        return proxy;
    }

    @Override
    public List<PlatformVersion> versions() {
        return versions;
    }

    @Override
    public void versions(List<PlatformVersion> versions) {
        this.versions = versions;
    }

    @Override
    public List<String> prepareSteps() {
        return prepareSteps;
    }

    @Override
    public PlatformBase base() {
        return base;
    }

    @Override
    public String preCacheBuilder() {
        return preCacheBuilder;
    }

    @Override
    public String parser() {
        return parser;
    }

    @Override
    public String hashType() {
        return hashType;
    }

    @Override
    public void addVersion(PlatformVersion version) {
        versions.add(version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlatformImpl platform)) {
            return false;
        }
        return name.equals(platform.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
