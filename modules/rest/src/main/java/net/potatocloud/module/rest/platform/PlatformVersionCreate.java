package net.potatocloud.module.rest.platform;

public record PlatformVersionCreate(String name, Boolean local, String downloadUrl, String fileHash, Boolean legacy) {}
