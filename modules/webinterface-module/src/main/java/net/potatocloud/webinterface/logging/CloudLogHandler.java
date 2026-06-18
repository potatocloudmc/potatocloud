package net.potatocloud.webinterface.logging;

import net.potatocloud.api.CloudAPI;
import org.jboss.logmanager.Level;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class CloudLogHandler extends Handler {

    private final CloudAPI cloudAPI;

    public CloudLogHandler(CloudAPI cloudAPI) {
        this.cloudAPI = cloudAPI;
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }

        String message = getFormatter() != null
                ? getFormatter().format(record)
                : record.getMessage();

        int level = record.getLevel().intValue();

        if (level >= Level.SEVERE.intValue()) {
            if (record.getThrown() != null) {
                cloudAPI.logger().exception(record.getThrown());
            } else {
                cloudAPI.logger().error(message);
            }
        } else if (level >= Level.WARNING.intValue()) {
            cloudAPI.logger().warn(message);
        } else if (level >= Level.INFO.intValue()) {
            cloudAPI.logger().info(message);
        } else {
            cloudAPI.logger().debug(message);
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }
}
