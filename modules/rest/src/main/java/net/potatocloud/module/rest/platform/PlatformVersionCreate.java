package net.potatocloud.module.rest.platform;

public record PlatformVersionCreate(String name, boolean local, String downloadUrl, String fileHash, boolean legacy) {}
