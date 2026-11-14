package net.potatocloud.module.rest.service;

public record CopyRequest(String template, String path, Boolean replaceExisting) {}
