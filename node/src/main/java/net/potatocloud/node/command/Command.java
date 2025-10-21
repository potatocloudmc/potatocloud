package net.potatocloud.node.command;

import lombok.Getter;
import net.potatocloud.node.Node;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public abstract class Command {

    private final String name;
    private final String description;
    private final List<String> aliases;

    private final Set<SubCommand> subCommands;

    protected Command() {
        final CommandInfo info = this.getClass().getAnnotation(CommandInfo.class);
        if (info == null) {
            throw new IllegalStateException("CommandInfo annotation missing in Command: " + getClass().getSimpleName());
        }
        name = info.name();
        description = info.description();
        aliases = Arrays.asList(info.aliases());
        subCommands = new HashSet<>();
    }

    public abstract void execute(String[] args);

    public SubCommand getSubCommand(String name) {
        return subCommands.stream().filter(subCommand -> subCommand.getName().equals(name)).findFirst().orElse(null);
    }

    public void addSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    protected void sendHelp() {
        for (SubCommand subCommand : subCommands) {
            Node.getInstance().getLogger().info(subCommand.getUsage() + " &8- &7" + subCommand.getDescription());
        }
    }
}
