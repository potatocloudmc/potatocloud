package net.potatocloud.node.setup.setups;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformBase;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.Setup;
import net.potatocloud.node.setup.answer.AnswerResult;

import java.util.ArrayList;
import java.util.Arrays;
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
                .suggestions(() -> Arrays.stream(PlatformBase.values())
                        .filter(base -> base != PlatformBase.UNKNOWN)
                        .map(PlatformBase::id)
                        .toList())
                .customValidator(input -> PlatformBase.fromId(input) != PlatformBase.UNKNOWN
                        ? AnswerResult.success()
                        : AnswerResult.error("This base is not supported"))
                .add();
    }

    @Override
    protected void finish(Map<String, String> answers) {
        final String name = answers.get("name");
        final PlatformBase platformBase = PlatformBase.fromId(answers.get("base"));

        boolean proxy = false;
        String preCache = null;
        List<String> prepareSteps = new ArrayList<>();

        switch (platformBase) {
            case PAPER -> {
                preCache = "paper";
                prepareSteps = List.of("default-files", "eula", "port", "setup-proxy");
            }
            case BUKKIT, SPIGOT -> prepareSteps = List.of("default-files", "eula", "port", "setup-proxy");
            case VELOCITY -> {
                proxy = true;
                prepareSteps = List.of("default-files", "port", "setup-forwarding");
            }
            case LIMBO -> prepareSteps = List.of("default-files", "port", "setup-proxy");
            case FABRIC -> {
                preCache = "fabric";
                prepareSteps = List.of("default-files", "eula", "port", "install-proxyforward");
            }
            case NEOFORGE -> {
                prepareSteps = List.of("default-files", "eula", "port", "install-proxyforward");
            }
            default -> {
            }
        }

        final Platform platform = platformManager.builder(name)
                .custom(true)
                .proxy(proxy)
                .base(platformBase)
                .preCacheBuilder(preCache)
                .prepareSteps(prepareSteps)
                .build();

        platformManager.create(platform);
        logger.info("&aTip&8: &7Add a version using&8: &aplatform version add " + name);
    }

    @Override
    public String getName() {
        return "Platform Configuration";
    }
}