package net.potatocloud.node.command.commands;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.command.CommandManager;

@CommandInfo(name = "help", description = "Shows all commands", aliases = {"?"})
public class HelpCommand extends Command {

    public HelpCommand(Logger logger, CommandManager commandManager) {
        defaultExecutor(ctx -> {
            for (Command command : commandManager.getCommands().values()) {
                logger.info("&8» &a" + command.getName() + getAliases(command) + " &8- " + "&7" + command.getDescription());
            }
        });
    }

    private String getAliases(Command command) {
        if (command.getAliases().isEmpty()) {
            return "";
        }
        return " &8[&7" + String.join(", ", command.getAliases()) + "&8]&7";
    }
}
