package net.potatocloud.plugins.optional.npc;

import net.potatocloud.common.config.Config;
import net.potatocloud.common.config.yaml.YamlConfig;
import net.potatocloud.plugins.optional.npc.action.ActionExecutor;
import net.potatocloud.plugins.optional.npc.command.NpcCommand;
import net.potatocloud.plugins.optional.npc.command.SubCommand;
import net.potatocloud.plugins.optional.npc.command.sub.ActionSubCommand;
import net.potatocloud.plugins.optional.npc.command.sub.CreateSubCommand;
import net.potatocloud.plugins.optional.npc.command.sub.DeleteSubCommand;
import net.potatocloud.plugins.optional.npc.command.sub.EditSubCommand;
import net.potatocloud.plugins.optional.npc.command.sub.HologramSubCommand;
import net.potatocloud.plugins.optional.npc.command.sub.InfoSubCommand;
import net.potatocloud.plugins.optional.npc.command.sub.ListSubCommand;
import net.potatocloud.plugins.optional.npc.command.sub.MoveHereSubCommand;
import net.potatocloud.plugins.optional.npc.command.sub.ReloadSubCommand;
import net.potatocloud.plugins.optional.npc.command.sub.TeleportSubCommand;
import net.potatocloud.plugins.optional.npc.connect.ServiceConnector;
import net.potatocloud.plugins.optional.npc.entity.NpcEntityService;
import net.potatocloud.plugins.optional.npc.listener.NpcChunkListener;
import net.potatocloud.plugins.optional.npc.listener.NpcInteractListener;
import net.potatocloud.plugins.optional.npc.listener.SelectorListener;
import net.potatocloud.plugins.optional.npc.npc.NpcService;
import net.potatocloud.plugins.optional.npc.npc.NpcStorage;
import net.potatocloud.plugins.optional.npc.selector.SelectorMenu;
import net.potatocloud.plugins.optional.npc.task.LookAtTask;
import net.potatocloud.plugins.optional.npc.task.UpdateTask;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.List;

public final class NpcPlugin extends JavaPlugin {

    private static final String FOLDER = "plugins/potatocloud-npc";

    private NpcEntityService entityService;

    @Override
    public void onEnable() {
        final Config config = new YamlConfig(Path.of(FOLDER, "config.yml"));
        final Config npcConfig = new YamlConfig(Path.of(FOLDER, "npcs.yml"));
        final MessagesConfig messages = new MessagesConfig(FOLDER);
        config.load();
        npcConfig.load();
        messages.load();

        entityService = new NpcEntityService(this);

        final NpcService npcService = new NpcService(new NpcStorage(npcConfig, getLogger()), entityService);
        final ServiceConnector connector = new ServiceConnector(messages);
        final SelectorMenu selectorMenu = new SelectorMenu(config, connector);
        final ActionExecutor actionExecutor = new ActionExecutor(selectorMenu, connector, messages);

        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new NpcInteractListener(entityService, actionExecutor), this);
        pluginManager.registerEvents(new NpcChunkListener(this, npcService, entityService), this);
        pluginManager.registerEvents(new SelectorListener(selectorMenu), this);

        final List<SubCommand> subCommands = List.of(
                new CreateSubCommand(npcService, config, messages),
                new DeleteSubCommand(npcService, messages),
                new ListSubCommand(npcService, messages),
                new InfoSubCommand(npcService, messages),
                new TeleportSubCommand(npcService, messages),
                new MoveHereSubCommand(npcService, messages),
                new EditSubCommand(npcService, messages),
                new HologramSubCommand(npcService, messages),
                new ActionSubCommand(npcService, messages),
                new ReloadSubCommand(npcService, config, messages));

        registerCommand("npc", List.of("cloudnpc", "potatonpc"), new NpcCommand(config, messages, subCommands));

        getServer().getScheduler().runTaskTimer(this, new LookAtTask(entityService, config), 1L, 2L);
        getServer().getScheduler().runTaskTimer(this, new UpdateTask(entityService, selectorMenu), 20L, 20L);

        npcService.load();
    }

    @Override
    public void onDisable() {
        if (entityService != null) {
            entityService.despawnAll();
        }
    }
}
