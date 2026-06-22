package net.potatocloud.node.screen;

import java.util.List;

public interface Screen {

    String name();

    void open();

    void close();

    List<String> logs();

    void append(String line);

    void subscribe(ScreenSubscriber subscriber);

    void unsubscribe(ScreenSubscriber subscriber);

}
