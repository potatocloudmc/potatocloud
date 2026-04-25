package net.potatocloud.node.setup.setups;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.Setup;
import net.potatocloud.node.setup.answer.AnswerResult;

import java.io.File;
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
                    final boolean usingLocalFile = answer.equalsIgnoreCase("false") || answer.equalsIgnoreCase("no");
                    if (usingLocalFile) {
                        new File("platforms/" + platform.getName() + "/" + answers.get("name")).mkdirs();
                    }
                })
                .add();

        text("local_ready", "Please copy your platform file to /platforms/"
                + platform.getName() + "/<version-name>"
                + " and name it " + platform.getName() + "-<version-name>.jar\n"
                + "Type 'done' when ready cancel' to cancel.")
                .customValidator(input -> input.equalsIgnoreCase("done")
                        ? AnswerResult.success()
                        : AnswerResult.error("Type done if you are ready or cancel to cancel"))
                .skipIf(answers ->
                        answers.get("use_download").equalsIgnoreCase("true") || answers.get("use_download").equalsIgnoreCase("yes")
                )
                .suggestions(() -> List.of("done", "cancel"))
                .add();

        bool("has_template", """
                Does the platform have a template URL with placeholders like {sha256}, {version}, {build}?
                Example: https://fill-data.papermc.io/v1/objects/{sha256}/paper-{version}-{build}.jar
                Check the platform file or type 'no' if unsure.
                """)
                .skipIf(answers -> {
                    final String useDownload = answers.getOrDefault("use_download", "false");
                    return !(useDownload.equalsIgnoreCase("true") || useDownload.equalsIgnoreCase("yes"));
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
                    final String useDownload = answers.getOrDefault("use_download", "false");
                    final String hasTemplate = answers.getOrDefault("has_template", "false");

                    return !(useDownload.equalsIgnoreCase("true") || useDownload.equalsIgnoreCase("yes"))
                            || (hasTemplate.equalsIgnoreCase("true") || hasTemplate.equalsIgnoreCase("yes"));
                })
                .add();

        bool("legacy", "Is this a legacy version? (1.8)")
                .add();
    }

    @Override
    protected void finish(Map<String, String> answers) {
        final boolean useDownload = Boolean.parseBoolean(answers.get("use_download"));

        final PlatformVersion version = new PlatformVersionImpl(
                platform.getName(),
                answers.get("name"),
                !useDownload,
                answers.get("download_url"),
                Boolean.parseBoolean(answers.get("legacy"))
        );

        platform.addVersion(version);
        platform.update();

        logger.info("Version &a" + version.getName() + " &7was added to platform &a" + platform.getName());
    }

    @Override
    public String getName() {
        return "Add Platform Version";
    }
}