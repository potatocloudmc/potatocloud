package net.potatocloud.network.protocol;

import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.request.RequestManager;
import net.potatocloud.network.request.ResponsePacket;

public record PacketContext<T extends Packet>(NetworkConnection connection, RequestManager requestManager, T packet, int requestId) {

    public boolean hasRequest() {
        return requestId != 0;
    }

    public void reply(ResponsePacket response) {
        if (!hasRequest()) {
            throw new IllegalStateException("No request available for reply()");
        }
        requestManager.requestId(response, requestId);
        connection.send(response);
        requestManager.removeRequest(packet);
    }
}
