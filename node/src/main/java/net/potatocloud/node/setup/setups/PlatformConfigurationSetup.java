package net.potatocloud.node.setup.setups;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.Setup;
import net.potatocloud.node.setup.answer.AnswerResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlatformConfigurationSetup extends Setup {

    private final PlatformManager platformManager;
    private final Logger logger;

    public PlatformConfigurationSetup(Console console, ScreenManager screenManager, PlatformManager platformManager, Logger logger) {
        super(console, screenManager);
        this.platformManager = platformManager;
        this.logger = logger;
    }

    @Override
    public void initQuestions() {
        text("name", "What is the name of the platform?")
                .customValidator(input -> platformManager.exists(input)
                        ? AnswerResult.error("A platform with the same name already exists")
                        : AnswerResult.success())
                .add();

        text("base", "What is the base of the platform?")
                .suggestions(() -> List.of("bukkit", "spigot", "paper", "velocity", "limbo"))
                .customValidator(input -> List.of("bukkit", "spigot", "paper", "velocity", "limbo").contains(input)
                        ? AnswerResult.success()
                        : AnswerResult.error("This base is not supported"))
                .add();
    }

    @Override
    protected void finish(Map<String, String> answers) {
        final String name = answers.get("name");
        final String base = answers.get("base");

        boolean proxy = false;
        String preCache = null;
        List<String> prepareSteps = new ArrayList<>();

        switch (base) {
            case "paper" -> {
                preCache = "paper";
                prepareSteps = List.of("default-files", "eula", "port", "setup-proxy");
            }
            case "purpur" -> {
                preCache = "purpur";
                prepareSteps = List.of("default-files", "eula", "port", "setup-proxy");
            }
            case "bukkit", "spigot" -> prepareSteps = List.of("default-files", "eula", "port", "setup-proxy");
            case "velocity" -> {
                proxy = true;
                prepareSteps = List.of("default-files", "port", "setup-forwarding");
            }
            case "limbo" -> prepareSteps = List.of("default-files", "port", "setup-proxy");
        }

        final Platform platform = platformManager.createPlatform(name, null, true, proxy, base, preCache, null, null, prepareSteps);
        logger.info("&aTip&8: &7Add a version using&8: &aplatform version add " + platform.getName());
    }

    @Override
    public String getName() {
        return "Platform Configuration";
    }
}