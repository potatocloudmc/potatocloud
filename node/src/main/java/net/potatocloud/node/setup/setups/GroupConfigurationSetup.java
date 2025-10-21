package net.potatocloud.node.setup.setups;

import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.node.Node;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.Setup;
import net.potatocloud.node.setup.SetupAnswerResult;
import net.potatocloud.node.setup.validator.BooleanValidator;
import net.potatocloud.node.setup.validator.IntegerValidator;
import net.potatocloud.node.utils.ProxyUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupConfigurationSetup extends Setup {

    private final ServiceGroupManager groupManager;
    private final PlatformManager platformManager;

    public GroupConfigurationSetup(Console console, ScreenManager screenManager, ServiceGroupManager groupManager, PlatformManager platformManager) {
        super(console, screenManager);
        this.groupManager = groupManager;
        this.platformManager = platformManager;
    }

    @Override
    public void initQuestions() {
        question("name")
                .question("What is the name of the group?")
                .validator(input -> {
                    if (input.isBlank()) {
                        return SetupAnswerResult.error("Name cannot be empty");
                    }
                    if (groupManager.existsServiceGroup(input)) {
                        return SetupAnswerResult.error("A service group with the same name already exists");
                    }
                    return SetupAnswerResult.success();
                })
                .done();

        question("platform")
                .question("What is the platform (server version) of the group?")
                .choices(answers -> platformManager.getPlatforms().stream()
                        .map(Platform::getName)
                        .collect(Collectors.toList()))
                .validator(input -> platformManager.exists(input) ? SetupAnswerResult.success() : SetupAnswerResult.error("This platform does not exist"))
                .done();

        question("platform_version")
                .question("Which version of the selected platform should be used?")
                .choices(answers -> {
                    final String platformName = answers.get("platform");
                    if (platformName == null) {
                        return List.of();
                    }
                    final Platform platform = platformManager.getPlatform(platformName);
                    if (platform == null) {
                        return List.of();
                    }
                    return platform.getVersions().stream().map(PlatformVersion::getName).collect(Collectors.toList());
                })
                .validator(input -> {
                    final String platformName = answers.get("platform");
                    final Platform platform = platformManager.getPlatform(platformName);
                    if (platform == null || platform.getVersion(input) == null) {
                        return SetupAnswerResult.error("This version does not exist for the selected platform");
                    }
                    return SetupAnswerResult.success();
                })
                .done();

        question("min_online_count")
                .question("What is the min online count of the group?")
                .defaultAnswer("1")
                .validator(new IntegerValidator())
                .done();

        question("max_online_count")
                .question("What is the max online count of the group?")
                .defaultAnswer("1")
                .validator(new IntegerValidator())
                .done();

        question("max_players")
                .question("What are the max players of the group?")
                .validator(new IntegerValidator())
                .done();

        question("max_memory")
                .question("What is the max memory of the group?")
                .choices(answers -> List.of("256", "512", "1024", "1536", "2048", "3072", "4096", "6144", "8192"))
                .validator(new IntegerValidator())
                .done();

        question("fallback")
                .question("Is this group a fallback?")
                .choices(answers -> List.of("true", "false", "yes", "no"))
                .validator(new BooleanValidator())
                .skipCondition(answers -> {
                    final String platformName = answers.get("platform");
                    if (platformName == null) {
                        return false;
                    }
                    final Platform platform = platformManager.getPlatform(platformName);
                    return platform != null && platform.isProxy();
                })
                .done();

        question("static_servers")
                .question("Is this group static?")
                .choices(answers -> List.of("true", "false", "yes", "no"))
                .validator(new BooleanValidator())
                .done();

        question("start_priority")
                .question("What is the start priority of the group? (higher = starts first)")
                .defaultAnswer("1")
                .validator(new IntegerValidator())
                .done();

        question("start_percentage")
                .question("At which percentage of online players should new services be started? (-1 = disabled)")
                .defaultAnswer("80")
                .validator(new IntegerValidator())
                .done();

        question("velocity_modern_forwarding")
                .question("Do you want to use Velocity modern forwarding? Modern forwarding is more secure but will break support for versions below 1.13")
                .choices(answers -> List.of("true", "false", "yes", "no"))
                .validator(new BooleanValidator())
                .skipCondition(answers -> {
                    final String platformName = answers.get("platform");
                    if (platformName == null) {
                        return true;
                    }
                    final Platform platform = platformManager.getPlatform(platformName);
                    return platform == null || !platform.isVelocityBased();
                })
                .done();

    }

    @Override
    protected void onFinish(Map<String, String> answers) {
        final String platformName = answers.get("platform");
        if (platformName == null) {
            return;
        }

        final Platform platform = platformManager.getPlatform(platformName);

        if (platform.isProxy() && ProxyUtils.getProxyGroups() != null && ProxyUtils.getProxyGroups().size() > 1) {
            Node.getInstance().getLogger().warn("You have more than one proxy group! This may cause issues");
        }

        final String name = answers.get("name");
        groupManager.createServiceGroup(
                name,
                answers.get("platform"),
                answers.get("platform_version"),
                Integer.parseInt(answers.get("min_online_count")),
                Integer.parseInt(answers.get("max_online_count")),
                Integer.parseInt(answers.get("max_players")),
                Integer.parseInt(answers.get("max_memory")),
                Boolean.parseBoolean(answers.getOrDefault("fallback", "false")),
                Boolean.parseBoolean(answers.get("static_servers")),
                Integer.parseInt(answers.get("start_priority")),
                Integer.parseInt(answers.get("start_percentage"))
        );

        final String modernForwarding = answers.get("velocity_modern_forwarding");
        if (modernForwarding != null) {
            final ServiceGroup group = groupManager.getServiceGroup(name);
            group.setProperty(DefaultProperties.VELOCITY_MODERN_FORWARDING, Boolean.parseBoolean(modernForwarding));
            group.update();
        }
    }

    @Override
    public String getName() {
        return "Group Configuration";
    }
}
