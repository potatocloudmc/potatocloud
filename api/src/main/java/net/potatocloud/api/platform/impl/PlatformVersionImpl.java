package net.potatocloud.api.platform.impl;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;

import java.util.Objects;

public class PlatformVersionImpl implements PlatformVersion {

    private final String platformName;
    private final String name;
    private final boolean local;
    private String resolvedName;
    private String downloadUrl;
    private String fileHash;
    private final boolean legacy;

    public PlatformVersionImpl(String platformName, String name, boolean local, String downloadUrl, boolean legacy) {
        this.platformName = platformName;
        this.name = name;
        this.local = local;
        this.downloadUrl = downloadUrl;
        this.legacy = legacy;
    }

    public PlatformVersionImpl(String platformName, String name, boolean local, String downloadUrl, String fileHash, boolean legacy) {
        this.platformName = platformName;
        this.name = name;
        this.local = local;
        this.downloadUrl = downloadUrl;
        this.fileHash = fileHash;
        this.legacy = legacy;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String resolvedName() {
        return resolvedName == null ? name : resolvedName;
    }

    public void resolvedName(String resolvedName) {
        this.resolvedName = resolvedName;
    }

    @Override
    public boolean local() {
        return local;
    }

    @Override
    public String downloadUrl() {
        return downloadUrl;
    }

    public void downloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String fileHash() {
        return fileHash;
    }

    public void fileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    @Override
    public boolean legacy() {
        return legacy;
    }

    @Override
    public Platform platform() {
        return CloudAPI.instance().platformManager().find(platformName).orElseThrow(() -> new IllegalStateException("Platform not found: " + platformName));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlatformVersionImpl other)) {
            return false;
        }
        return platformName.equals(other.platformName) && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platformName, name);
    }
}
