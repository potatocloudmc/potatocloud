package net.potatocloud.node.setup.setups;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.Setup;
import net.potatocloud.node.setup.SetupAnswerResult;
import net.potatocloud.node.setup.validator.BooleanValidator;

import java.util.Map;

public class AddVersionToPlatformSetup extends Setup {

    private final Platform platform;
    private final Logger logger;

    public AddVersionToPlatformSetup(Console console, ScreenManager screenManager, Platform platform, Logger logger) {
        super(console, screenManager);
        this.platform = platform;
        this.logger = logger;
    }

    @Override
    public void initQuestions() {
        question("name")
                .question("What is the name of the version?")
                .validator(input -> {
                    if (input.isBlank()) {
                        return SetupAnswerResult.error("Name cannot be empty");
                    }
                    if (platform.hasVersion(input)) {
                        return SetupAnswerResult.error("This version already exists for this platform");
                    }
                    return SetupAnswerResult.success();
                })
                .done();

        question("has_template")
                .question("Does the platform has a template URL with placeholders like {sha256}, {version}, {build}? \n" +
                        "Example: https://fill-data.papermc.io/v1/objects/{sha256}/paper-{version}-{build}.jar \n" +
                        "Check the platform file or type 'no' if unsure")
                .validator(new BooleanValidator())
                .done();

        question("download_url")
                .question("What is the Download URL of this version?")
                .validator(input -> {
                    if (input.isBlank()) {
                        return SetupAnswerResult.error("Download URL cannot be empty");
                    }
                    if (!input.startsWith("http://") && !input.startsWith("https://")) {
                        return SetupAnswerResult.error("Download URL must start with 'http://' or 'https://'");
                    }
                    return SetupAnswerResult.success();
                })
                .skipCondition(answers ->
                        answers.get("has_template").equalsIgnoreCase("true") || answers.get("has_template").equalsIgnoreCase("yes"))
                .done();

        question("legacy")
                .question("Is this a legacy version? (1.8)")
                .validator(new BooleanValidator())
                .done();
    }

    @Override
    protected void onFinish(Map<String, String> answers) {
        final String downloadUrl = answers.get("download_url") != null ? answers.get("download_url") : null;

        final PlatformVersion version = new PlatformVersionImpl(platform.getName(), answers.get("name"), downloadUrl, Boolean.parseBoolean(answers.get("legacy")));

        platform.addVersion(version);
        platform.update();

        logger.info("Version &a" + version.getName() + " &7was added to platform &a" + platform.getName());
    }

    @Override
    public String getName() {
        return "Add Version";
    }
}
