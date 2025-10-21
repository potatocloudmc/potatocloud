package net.potatocloud.api.platform.impl;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.potatocloud.api.platform.PlatformVersion;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(of = {"platformName", "name"})
public class PlatformVersionImpl implements PlatformVersion {

    private final String platformName;
    private final String name;
    private String downloadUrl;
    private String fileHash;
    private boolean legacy;

    public PlatformVersionImpl(String platformName, String name, String downloadUrl, boolean legacy) {
        this.platformName = platformName;
        this.name = name;
        this.downloadUrl = downloadUrl;
        this.legacy = legacy;
    }
}
