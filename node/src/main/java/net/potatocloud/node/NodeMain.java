package net.potatocloud.node;

public final class NodeMain {

    static void main() {
        Node node = new Node(System.currentTimeMillis());
        node.start();

        Runtime.getRuntime().addShutdownHook(new Thread(node::shutdown));
    }
}
