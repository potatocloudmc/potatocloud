package net.potatocloud.node.utils;

import java.io.IOException;
import java.net.ServerSocket;

public final class NetworkUtils {

    private NetworkUtils() {
    }

    public static boolean isPortFree(int port) {
        try (ServerSocket _ = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
