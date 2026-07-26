package net.potatocloud.node.utils;

import tools.jackson.databind.JsonNode;

public final class GitHubClient {

    private static final String API_URL = "https://api.github.com";

    private GitHubClient() {
    }

    public static JsonNode latestRelease(String repository) {
        return RequestUtil.request(API_URL + "/repos/" + repository + "/releases/latest");
    }
}
