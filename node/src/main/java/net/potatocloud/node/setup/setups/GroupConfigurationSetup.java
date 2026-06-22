package net.potatocloud.node.setup.setups;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.node.Node;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.Setup;
import net.potatocloud.node.setup.answer.AnswerResult;
import net.potatocloud.node.utils.ProxyUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupConfigurationSetup extends Setup {

    private final GroupManager groupManager;
    private final PlatformManager platformManager;

    public GroupConfigurationSetup(Console console, ScreenManager screenManager, GroupManager groupManager, PlatformManager platformManager) {
        super(console, screenManager);
        this.groupManager = groupManager;
        this.platformManager = platformManager;
    }

    @Override
    public void initQuestions() {
        text("name", "What is the name of this group?")
                .customValidator(input -> groupManager.exists(input)
                        ? AnswerResult.error("A group with the same name already exists")
                        : AnswerResult.success())
                .add();

        text("platform", "Which platform should be used by this group?")
                .suggestions(() -> platformManager.platforms().stream()
                        .map(Platform::name)
                        .collect(Collectors.toList()))
                .customValidator(input -> platformManager.exists(input)
                        ? AnswerResult.success()
                        : AnswerResult.error("This platform does not exist"))
                .add();

        text("platform_version", "Which version of the selected platform should be used?")
                .suggestions(() -> platformManager.find(answers.get("platform"))
                        .map(platform -> platform.versions().stream().map(PlatformVersion::name).collect(Collectors.toList()))
                        .orElse(List.of()))
                .customValidator(input -> platformManager.find(answers.get("platform"))
                        .filter(platform -> platform.hasVersion(input))
                        .map(_ -> AnswerResult.success())
                        .orElseGet(() -> AnswerResult.error("This version does not exist for the selected platform")))
                .add();

        number("min_online_count", "How many services of this group should always be online?")
                .defaultAnswer("1")
                .add();

        number("max_online_count", "What is the maximum number of online services in this group?")
                .defaultAnswer("1")
                .add();

        number("max_players", "What is the maximum number of players per service?")
                .add();

        number("max_memory", "What is the maximum memory a service can use in this group? (In MB)")
                .suggestions(() -> List.of("256", "512", "1024", "1536", "2048", "3072", "4096", "6144", "8192"))
                .add();

        bool("fallback", "Is this group a fallback?")
                .skipIf(answers -> platformManager.find(answers.get("platform"))
                        .map(Platform::proxy)
                        .orElse(false))
                .add();

        bool("static_servers", "Are services in this group static? (Service files will not be deleted on shutdown)")
                .add();

        number("start_priority", "What is the start priority of this group? (higher = starts first)")
                .defaultAnswer("1")
                .add();

        number("start_percentage", "At which percentage of online players should new services be started? (-1 = disabled)")
                .defaultAnswer("80")
                .add();

        bool("velocity_modern_forwarding", "Do you want to use Velocity modern forwarding? Modern forwarding is more secure but will break support for versions below 1.13")
                .skipIf(answers -> platformManager.find(answers.get("platform"))
                        .map(platform -> !platform.velocityBased())
                        .orElse(false))
                .add();
    }

    @Override
    protected void finish(Map<String, String> answers) {
        final String platformName = answers.get("platform");
        if (platformName == null) {
            return;
        }

        platformManager.find(platformName)
                .filter(platform -> platform.proxy() && !ProxyUtils.getProxyGroups().isEmpty())
                .ifPresent(_ -> Node.instance().logger().warn("You have more than one proxy group! This may cause issues"));

        final String name = answers.get("name");
        final Group group = groupManager.builder(name)
                .node(Node.instance().config().cluster().name()) // todo ugly
                .platform(answers.get("platform"))
                .platformVersion(answers.get("platform_version"))
                .minServices(Integer.parseInt(answers.get("min_online_count")))
                .maxServices(Integer.parseInt(answers.get("max_online_count")))
                .maxPlayers(Integer.parseInt(answers.get("max_players")))
                .maxMemory(Integer.parseInt(answers.get("max_memory")))
                .fallback(Boolean.parseBoolean(answers.getOrDefault("fallback", "false")))
                .staticServices(Boolean.parseBoolean(answers.get("static_servers")))
                .startPriority(Integer.parseInt(answers.get("start_priority")))
                .startPercentage(Integer.parseInt(answers.get("start_percentage")))
                .build();

        groupManager.create(group);

        final String modernForwarding = answers.get("velocity_modern_forwarding");
        if (modernForwarding != null) {
            groupManager.find(name).ifPresent(created -> {
                created.set(DefaultProperties.VELOCITY_MODERN_FORWARDING, Boolean.parseBoolean(modernForwarding));
                groupManager.update(created);
            });
        }
    }

    @Override
    public String getName() {
        return "Group Configuration";
    }
}