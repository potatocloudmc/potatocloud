package net.potatocloud.api.platform.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.potatocloud.api.platform.PlatformVersion;

@Getter
@Setter
@AllArgsConstructor
public class PlatformVersionImpl implements PlatformVersion {

    private final String platformName;
    private final String name;
    private String downloadUrl;
    private final String parser;
    private final String hashType;
    private String fileHash;
    private boolean legacy;

    public PlatformVersionImpl(String platformName, String name, String downloadUrl, String parser, String hashType, boolean legacy) {
        this.platformName = platformName;
        this.name = name;
        this.downloadUrl = downloadUrl;
        this.parser = parser;
        this.hashType = hashType;
        this.legacy = legacy;
    }
}
