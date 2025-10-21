package net.potatocloud.api.platform;

import net.potatocloud.api.CloudAPI;

public interface PlatformVersion {

    String getPlatformName();

    String getName();

    String getDownloadUrl();

    String getFileHash();

    boolean isLegacy();

    default String getFullName() {
        return getPlatformName() + "-" + getName();
    }

    default Platform getPlatform() {
        return CloudAPI.getInstance().getPlatformManager().getPlatform(getPlatformName());
    }
}
