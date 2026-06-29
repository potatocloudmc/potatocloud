package net.potatocloud.network.exception;

public class PacketTooBigException extends Exception {

    public PacketTooBigException(int length) {
        super("Received a packet that is too big! (" + length + ")");
    }
}
