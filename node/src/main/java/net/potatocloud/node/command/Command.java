package net.potatocloud.node.command;

import lombok.Getter;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.node.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class Command {

    private final String name;
    private final String description;
    private final List<String> aliases;

    private final List<SubCommand> subCommands = new ArrayList<>();
    private Consumer<CommandContext> defaultExecutor;

    protected Command() {
        final CommandInfo info = this.getClass().getAnnotation(CommandInfo.class);
        if (info == null) {
            throw new IllegalStateException("CommandInfo annotation missing in Command: " + getClass().getSimpleName());
        }

        this.name = info.name();
        this.description = info.description();
        this.aliases = Arrays.asList(info.aliases());
    }

    public void defaultExecutor(Consumer<CommandContext> executor) {
        this.defaultExecutor = executor;
    }

    public SubCommand sub(String name) {
        return sub(name, null);
    }

    public SubCommand sub(String name, String description) {
        final SubCommand sub = new SubCommand(name, description, this);
        subCommands.add(sub);
        return sub;
    }

    public void execute(String[] args) {
        final CommandContext ctx = new CommandContext();

        if (args.length == 0) {
            if (defaultExecutor != null) {
                defaultExecutor.accept(ctx);
            }
            return;
        }

        final String arg = args[0];
        for (SubCommand sub : subCommands) {
            if (sub.getName().equalsIgnoreCase(arg)) {
                sub.execute(args, 1, ctx);
                return;
            }
        }

        if (defaultExecutor != null) {
            defaultExecutor.accept(ctx);
        }
    }

    protected void sendHelp() {
        final Logger logger = Node.getInstance().getLogger();
        for (SubCommand sub : subCommands) {
            logger.info("&8» &a" + name + " " + sub.getName() + " &8- &7" + (sub.getDescription() != null ? sub.getDescription() : ""));
        }
    }
}
