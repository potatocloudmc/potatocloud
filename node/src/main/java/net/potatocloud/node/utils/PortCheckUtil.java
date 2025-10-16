package net.potatocloud.node.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.Socket;

@UtilityClass
public  class PortCheckUtil {

    public boolean isPortInUse(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
