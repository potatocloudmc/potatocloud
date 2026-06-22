package net.potatocloud.node.setup.setups;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.Node;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.Setup;
import net.potatocloud.node.setup.answer.AnswerResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
        text("name", "What is the name of the version?")
                .customValidator(input -> platform.hasVersion(input)
                        ? AnswerResult.error("This version already exists for this platform")
                        : AnswerResult.success())
                .add();

        bool("use_download", """
                Should this version be downloaded automatically?
                
                Type 'yes' to use a download URL.
                Type 'no' if you want to add the JAR file yourself.
                """)
                .answerAction((answers, answer) -> {
                    final boolean useDownload = Boolean.parseBoolean(answer);

                    if (!useDownload) {
                        try {
                            final String name = answers.get("name");

                            if (name.contains("..") || name.contains("/") || name.contains("\\")) {
                                throw new IllegalArgumentException("Can not create version directory with invalid characters");
                            }

                            Files.createDirectories(Path.of("platforms", platform.name(), name));
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create platform directory", e);
                        }
                    }
                })
                .add();

        text("local_ready", "Please copy your platform file to /platforms/"
                + platform.name() + "/<version-name>"
                + " and name it " + platform.name() + "-<version-name>.jar\n"
                + "Type 'done' when ready cancel' to cancel.")
                .customValidator(input -> input.equalsIgnoreCase("done")
                        ? AnswerResult.success()
                        : AnswerResult.error("Type done if you are ready or cancel to cancel"))
                .skipIf(answers -> Boolean.parseBoolean(answers.get("use_download")))
                .suggestions(() -> List.of("done", "cancel"))
                .add();

        bool("has_template", """
                Does the platform have a template URL with placeholders like {sha256}, {version}, {build}?
                Example: https://fill-data.papermc.io/v1/objects/{sha256}/paper-{version}-{build}.jar
                Check the platform file or type 'no' if unsure.
                """)
                .skipIf(answers -> {
                    final String useDownload = answers.get("use_download");
                    return !(useDownload.equalsIgnoreCase("true"));
                })
                .add();

        text("download_url", "What is the download URL of this version?")
                .customValidator(input -> {
                    if (!input.startsWith("http://") && !input.startsWith("https://")) {
                        return AnswerResult.error("Download URL must start with 'http://' or 'https://'");
                    }
                    return AnswerResult.success();
                })
                .skipIf(answers -> {
                    final String useDownload = answers.get("use_download");
                    final String hasTemplate = answers.get("has_template");
                    return !(useDownload.equalsIgnoreCase("true") || (hasTemplate.equalsIgnoreCase("true")));
                })
                .add();

        bool("legacy", "Is this a legacy version? (1.8)")
                .add();
    }

    @Override
    protected void finish(Map<String, String> answers) {
        final boolean useDownload = Boolean.parseBoolean(answers.get("use_download"));

        final PlatformVersion version = new PlatformVersionImpl(
                platform.name(),
                answers.get("name"),
                !useDownload,
                answers.get("download_url"),
                Boolean.parseBoolean(answers.get("legacy"))
        );

        platform.addVersion(version);
        Node.instance().platformManager().update(platform);

        logger.info("Version &a" + version.name() + " &7was added to platform &a" + platform.name());
    }

    @Override
    public String getName() {
        return "Add Platform Version";
    }
}