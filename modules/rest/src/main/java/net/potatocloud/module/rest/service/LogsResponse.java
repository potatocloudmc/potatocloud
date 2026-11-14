package net.potatocloud.module.rest.service;

import java.util.List;

public record LogsResponse(String service, List<String> lines) {}
