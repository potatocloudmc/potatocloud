package net.potatocloud.webinterface.logging;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import net.potatocloud.api.CloudAPI;

import java.util.logging.Logger;

@ApplicationScoped
public class LoggingInitializer {

    void onStart(@Observes StartupEvent event) {
        Logger rootLogger = Logger.getLogger("");
        for (java.util.logging.Handler h : rootLogger.getHandlers()) {
            rootLogger.removeHandler(h);
        }
        rootLogger.addHandler(new CloudLogHandler(CloudAPI.instance()));
    }

}
