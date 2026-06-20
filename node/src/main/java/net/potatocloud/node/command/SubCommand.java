package net.potatocloud.node.command;

import lombok.Getter;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.arguments.MultiStringArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class SubCommand {

    private final String name;
    private final String description;
    private final Command parent;
    private final List<SubCommand> subCommands = new ArrayList<>();
    private final List<SubCommand> parentSubCommands = new ArrayList<>();
    private final List<ArgumentType<?>> arguments = new ArrayList<>();
    private Consumer<CommandContext> executor;
    private CommandCompleter completer;

    public SubCommand(String name, String description, Command parent) {
        this.name = name;
        this.description = description;
        this.parent = parent;
    }

    public SubCommand argument(ArgumentType<?> argument) {
        if (!arguments.isEmpty() && !arguments.getLast().isRequired()) {
            throw new IllegalStateException("Cannot add arguments after a optional argument");
        }

        if (!arguments.isEmpty() && arguments.getLast() instanceof MultiStringArgument) {
            throw new IllegalStateException("Cannot add arguments after a MultiStringArgument");
        }

        arguments.add(argument);
        return this;
    }

    public SubCommand optionalArgument(ArgumentType<?> argument) {
        argument(argument.asOptionalArgument());
        return this;
    }

    public SubCommand suggests(CommandCompleter completer) {
        this.completer = completer;
        return this;
    }

    public SubCommand executes(Consumer<CommandContext> executor) {
        this.executor = executor;
        return this;
    }

    public SubCommand sub(String name) {
        return sub(name, null);
    }

    public SubCommand sub(String name, String description) {
        final SubCommand sub = new SubCommand(name, description, parent);
        sub.getParentSubCommands().add(this);
        subCommands.add(sub);
        return sub;
    }

    public void execute(String[] args, int index, CommandContext ctx) {
        final CommandContext.ParseResult parsed = buildContext(args, index);

        if (!parsed.isComplete()) {
            final String errorMessage = parsed.getErrorMessage();
            if (errorMessage == null) {
                sendHelp();
                return;
            }
            Node.instance().logger().info(errorMessage);
            return;
        }

        ctx.getValues().putAll(parsed.getContext().getValues());

        final int nextIndex = index + parsed.getParsedArguments();
        if (nextIndex < args.length) {
            final String next = args[nextIndex];

            for (SubCommand sub : subCommands) {
                if (sub.getName().equalsIgnoreCase(next)) {
                    sub.execute(args, nextIndex + 1, ctx);
                    return;
                }
            }
        }

        if (executor != null) {
            executor.accept(ctx);
        }
    }

    public List<String> suggest(CommandContext ctx, String input, int argsLength) {
        if (completer == null) {
            return List.of();
        }
        return completer.complete(ctx, input, argsLength);
    }

    public CommandContext.ParseResult buildContext(String[] args, int startIndex) {
        final CommandContext ctx = new CommandContext();
        int parsed = 0;

        for (ArgumentType<?> argument : arguments) {
            final int idx = startIndex + parsed;

            if (argument instanceof MultiStringArgument multiStringArgument) {
                final String combined = multiStringArgument.combineArguments(args, idx);

                ctx.set(argument.getName(), combined);
                parsed += args.length - idx;
                break;
            }

            // Check if the user has passed enough arguments
            if (idx >= args.length) {
                if (argument.isRequired()) {
                    return new CommandContext.ParseResult(ctx, parsed, false, getUsageMessage());
                }
                // Argument is optional, its fine that its missing
                continue;
            }

            // Try to parse the argument
            final ArgumentType.ParseResult<?> result = argument.parse(args[idx]);

            if (!result.isSuccess()) {
                return new CommandContext.ParseResult(ctx, parsed, false, result.getError().getMessage());
            }

            ctx.set(argument.getName(), result.getValue());
            parsed++;
        }

        return new CommandContext.ParseResult(ctx, parsed, true);
    }

    public void sendHelp() {
        Node.instance().logger().info(getUsageMessage());
    }

    public String getUsageMessage() {
        final StringBuilder builder = new StringBuilder();
        builder.append("&cUse&8: &7");

        builder.append(parent.getName()).append(" ");

        for (SubCommand parentSub : parentSubCommands) {
            builder.append(parentSub.getName()).append(" ");
        }

        builder.append(name);

        for (ArgumentType<?> argument : arguments) {
            builder.append(" &8[&a").append(argument.getName());

            if (!argument.isRequired()) {
                builder.append("?");
            }

            if (argument instanceof MultiStringArgument) {
                builder.append("...");
            }

            builder.append("&8]");
        }

        if (!subCommands.isEmpty()) {
            builder.append(" &8[&a");
            for (int i = 0; i < subCommands.size(); i++) {
                builder.append(subCommands.get(i).getName());
                if (i < subCommands.size() - 1) {
                    builder.append("&8|&a");
                }
            }
            builder.append("&8]");
        }

        return builder.toString();
    }
}
