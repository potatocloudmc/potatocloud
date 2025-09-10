package net.potatocloud.api.platform;

public interface PlatformVersion {

    String getPlatformName();

    String getName();

    String getDownloadUrl();

    String getParser();

    String getHashType();

    String getFileHash();

    boolean isLegacy();

    default String getFullName() {
        return getPlatformName() + "-" + getName();
    }
}
