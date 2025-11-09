package net.potatocloud.node.setup.setups;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.AnswerResult;
import net.potatocloud.node.setup.Setup;

import java.io.File;
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
                .text("What is the name of the version?")
                .customValidator(input -> {
                    if (platform.hasVersion(input)) {
                        return AnswerResult.error("This version already exists for this platform");
                    }
                    return AnswerResult.success();
                })
                .add();

        question("use_download")
                .bool("""
                        Should this version be downloaded automatically?
                        
                        Type 'yes' to use a download URL.
                        Type 'no' if you want to add the JAR file yourself.
                        """)
                .answerAction((answers, answer) -> {
                    // only if using local platform file
                    if (answer.equalsIgnoreCase("false") || answer.equalsIgnoreCase("no")) {
                        final File platformFolder = new File("platforms/" + platform.getName() + "/" + answer);
                        platformFolder.mkdirs();
                    }

                })
                .add();

        question("local_ready")
                .text("Please copy your platform file to this folder /platforms/"
                        + platform.getName() + "/<version-name>"
                        + " and name it "
                        + platform.getName() + "-<version-name>.jar\n"
                        + "Type 'done' when ready or 'cancel' to cancel."
                )
                .customValidator(input -> {
                    if (!input.equalsIgnoreCase("done") && !input.equalsIgnoreCase("cancel")) {
                        return AnswerResult.error("Type done if you are ready or cancel to cancel");
                    }
                    return AnswerResult.success();
                })
                .skipIf(answers ->
                        answers.get("use_download").equalsIgnoreCase("true") || answers.get("use_download").equalsIgnoreCase("yes")
                )
                .add();

        question("has_template")
                .bool("""
                        Does the platform have a template URL with placeholders like {sha256}, {version}, {build}?
                        Example: https://fill-data.papermc.io/v1/objects/{sha256}/paper-{version}-{build}.jar
                        Check the platform file or type 'no' if unsure.
                        """)
                .skipIf(answers -> {
                    final String useDownload = answers.getOrDefault("use_download", "false");
                    return !(useDownload.equalsIgnoreCase("true") || useDownload.equalsIgnoreCase("yes"));
                })
                .add();

        question("download_url")
                .text("What is the Download URL of this version?")
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

        question("legacy")
                .bool("Is this a legacy version? (1.8)")
                .add();
    }

    @Override
    protected void onFinish(Map<String, String> answers) {
        final boolean useDownload = Boolean.parseBoolean(answers.get("use_download"));
        final boolean isLocal = !useDownload;
        final String downloadUrl = answers.get("download_url");

        final PlatformVersion version = new PlatformVersionImpl(
                platform.getName(),
                answers.get("name"),
                isLocal,
                downloadUrl,
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
