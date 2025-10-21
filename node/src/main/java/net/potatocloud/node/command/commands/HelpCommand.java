package net.potatocloud.node.command.commands;

import lombok.RequiredArgsConstructor;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.command.CommandManager;
import net.potatocloud.node.console.Logger;

@RequiredArgsConstructor
@CommandInfo(name = "help", description = "Shows all commands", aliases = {"?"})
public class HelpCommand extends Command {

    private final Logger logger;
    private final CommandManager commandManager;

    @Override
    public void execute(String[] args) {
        for (Command command : commandManager.getCommands().values()) {
            logger.info("&8» &a" + command.getName() + getAliases(command) + " &8- " + "&7" + command.getDescription());
        }
    }

    private String getAliases(Command command) {
        if (command.getAliases().isEmpty()) {
            return "";
        }
        return " &8[&7" + String.join(", ", command.getAliases()) + "&8]&7";
    }
}
