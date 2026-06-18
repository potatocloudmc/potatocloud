package net.potatocloud.webinterface;

import io.quarkus.runtime.Quarkus;
import lombok.Getter;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.module.AbstractModule;
import net.potatocloud.common.config.Config;
import net.potatocloud.common.config.yaml.YamlConfig;
import net.potatocloud.webinterface.logging.CloudLogHandler;

import java.nio.file.Path;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class WebInterfaceModule extends AbstractModule {

    @Getter
    public static WebInterfaceModule instance;
    @Getter
    public CloudAPI cloudAPI;

    private Thread quarkusThread;
    private volatile boolean started = false;

    @Override
    public void onEnable() {
        instance = this;
        cloudAPI = CloudAPI.instance();

        Config config = new YamlConfig(
                Path.of("modules", "webinterface").resolve("wi-config.yml"),
                getClass().getClassLoader()
        );
        config.load();

        this.injectCloudLogger();

        applyQuarkusOverrides(config);

        ClassLoader pluginClassLoader = getClass().getClassLoader();

        quarkusThread = new Thread(() -> {
            ClassLoader previous = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(pluginClassLoader);
            try {
                Quarkus.run(
                        WebInterfaceQuarkusApp.class,
                        (exitCode, throwable) -> {
                            started = false;
                            if (throwable != null) {
                                cloudAPI.logger().exception(throwable);
                            }
                        }
                );
            } finally {
                Thread.currentThread().setContextClassLoader(previous);
            }
        }, "webinterface-quarkus");

        quarkusThread.setDaemon(true);
        quarkusThread.start();
        started = true;

        cloudAPI.logger().info("Starting WebInterface module...");
    }

    private void applyQuarkusOverrides(Config config) {
        setIfPresent(config, "port", "quarkus.http.port");
        setIfPresent(config, "bind-address", "quarkus.http.host");

        setIfPresent(config, "api-keys", "app.security.api-keys");
        setIfPresent(config, "webhook-secret", "app.security.webhook.secret");
        setIfPresent(config, "jwt-secret", "app.security.jwt.secret");
    }

    private void setIfPresent(Config config, String yamlKey, String quarkusProperty) {
        var value = config.get(yamlKey);
        if (value != null && value.asString() != null && !value.asString().isBlank()) {
            System.setProperty(quarkusProperty, value.asString());
        }
    }

    @Override
    public void onDisable() {
        if (started) {
            Quarkus.asyncExit(0);
            try {
                quarkusThread.join(15_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        cloudAPI.logger().info("WebInterface module stopped.");
    }

    private void injectCloudLogger() {
        Logger rootLogger = Logger.getLogger("");
        for (Handler h : rootLogger.getHandlers()) {
            rootLogger.removeHandler(h);
        }

        rootLogger.addHandler(new CloudLogHandler(cloudAPI));
    }

}
