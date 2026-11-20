package net.potatocloud.node.setup.setups;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.property.DefaultProperties;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.setup.Setup;

import java.util.Map;

public final class FirstStartSetup extends Setup {

    private final GroupManager groupManager;
    private final Logger logger;
    private final String nodeName;
    private final String groupsFolder;

    public FirstStartSetup(Console console, ScreenManager screenManager, GroupManager groupManager, Logger logger, String nodeName, String groupsFolder) {
        super(console, screenManager);
        this.groupManager = groupManager;
        this.logger = logger;
        this.nodeName = nodeName;
        this.groupsFolder = groupsFolder;
    }

    @Override
    public void initQuestions() {
        bool("create_default_groups", """
                Should a basic network be set up for you?
                
                  &8• &7Proxy &8(&7Velocity, 512 MB, 1 service&8)
                  &8• &7Lobby &8(&7Paper, 2048 MB, 1-2 services, fallback&8)
                
                &7Type yes to create both groups with default values, or no to create your own groups later""")
                .defaultAnswer("yes")
                .add();
    }

    @Override
    protected void finish(Map<String, String> answers) {
        if (!Boolean.parseBoolean(answers.get("create_default_groups"))) {
            logger.info("No default groups were created. Use &agroup create &7to create your first group");
            return;
        }

        final Group proxy = groupManager.builder("Proxy")
                .node(nodeName)
                .platform("velocity")
                .platformVersion("latest")
                .minServices(1)
                .maxServices(1)
                .maxPlayers(100)
                .maxMemory(512)
                .fallback(false)
                .staticServices(false)
                .startPriority(2)
                .startPercentage(-1)
                .property(DefaultProperties.VELOCITY_MODERN_FORWARDING, true)
                .build();

        groupManager.create(proxy);

        final Group lobby = groupManager.builder("Lobby")
                .node(nodeName)
                .platform("paper")
                .platformVersion("latest")
                .minServices(1)
                .maxServices(2)
                .maxPlayers(50)
                .maxMemory(2048)
                .fallback(true)
                .staticServices(false)
                .startPriority(1)
                .startPercentage(80)
                .build();

        groupManager.create(lobby);

        logger.info("Created the groups &aProxy &7and &aLobby");
        logger.info("You can edit the groups with the &agroup edit &7command or in the &a" + groupsFolder + " &7folder");
    }

    @Override
    public String getName() {
        return "First Start";
    }
}
