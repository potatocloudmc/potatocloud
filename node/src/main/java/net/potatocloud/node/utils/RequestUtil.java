package net.potatocloud.node.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.node.Node;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@UtilityClass
public class RequestUtil {

    public JsonObject request(String url) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            final HttpRequest buildRequest = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "potatocloud/" + CloudAPI.VERSION + " (https://github.com/potatocloudmc/potatocloud)")
                    .build();

            final HttpResponse<String> buildResponse = client.send(buildRequest, HttpResponse.BodyHandlers.ofString());

            return JsonParser.parseString(buildResponse.body()).getAsJsonObject();
        } catch (Exception e) {
            Node.getInstance().getLogger().info("Failed to request: " + url);
        }
        return null;
    }
}
