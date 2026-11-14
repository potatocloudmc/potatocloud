package net.potatocloud.node.command;

import net.potatocloud.node.Node;
import net.potatocloud.node.command.arguments.MultiStringArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
        if (!arguments.isEmpty() && !arguments.getLast().required()) {
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
        sub.parentSubCommands().add(this);
        subCommands.add(sub);
        return sub;
    }

    public void execute(String[] args, int index, CommandContext ctx) {
        final CommandContext.ParseResult parsed = buildContext(args, index);

        if (!parsed.complete()) {
            final String errorMessage = parsed.errorMessage();
            if (errorMessage == null) {
                sendHelp();
                return;
            }
            Node.instance().logger().info(errorMessage);
            return;
        }

        ctx.values().putAll(parsed.context().values());

        final int nextIndex = index + parsed.parsedArguments();
        if (nextIndex < args.length) {
            final String next = args[nextIndex];

            for (SubCommand sub : subCommands) {
                if (sub.name().equalsIgnoreCase(next)) {
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

                ctx.set(argument.name(), combined);
                parsed += args.length - idx;
                break;
            }

            // Check if the user has passed enough arguments
            if (idx >= args.length) {
                if (argument.required()) {
                    return new CommandContext.ParseResult(ctx, parsed, false, getUsageMessage());
                }
                // Argument is optional, its fine that its missing
                continue;
            }

            // Try to parse the argument
            final ArgumentType.ParseResult<?> result = argument.parse(args[idx]);

            if (!result.isSuccess()) {
                return new CommandContext.ParseResult(ctx, parsed, false, result.error().message());
            }

            ctx.set(argument.name(), result.value());
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

        builder.append(parent.name()).append(" ");

        for (SubCommand parentSub : parentSubCommands) {
            builder.append(parentSub.name()).append(" ");
        }

        builder.append(name);

        for (ArgumentType<?> argument : arguments) {
            builder.append(" &8[&a").append(argument.name());

            if (!argument.required()) {
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
                builder.append(subCommands.get(i).name());
                if (i < subCommands.size() - 1) {
                    builder.append("&8|&a");
                }
            }
            builder.append("&8]");
        }

        return builder.toString();
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public List<SubCommand> subCommands() {
        return subCommands;
    }

    public List<SubCommand> parentSubCommands() {
        return parentSubCommands;
    }

    public List<ArgumentType<?>> arguments() {
        return arguments;
    }
}
