package net.potatocloud.node.command;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.service.Service;
import net.potatocloud.node.command.arguments.*;

import java.util.List;

public abstract class ArgumentType<T> {

    private final String name;
    private final boolean required;

    public ArgumentType(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    public ArgumentType(String name) {
        this.name = name;
        this.required = true;
    }

    public String name() {
        return name;
    }

    public boolean required() {
        return required;
    }

    public abstract ParseResult<T> parse(String input);

    public List<String> suggest(String input) {
        return List.of();
    }

    public static ArgumentType<String> String(String name) {
        return new StringArgument(name);
    }

    public static ArgumentType<String> MultiString(String name) {
        return new MultiStringArgument(name);
    }

    public static ArgumentType<Integer> Integer(String name) {
        return new IntegerArgument(name);
    }

    public static ArgumentType<Group> Group(String name) {
        return new GroupArgument(name);
    }

    public static ArgumentType<Service> Service(String name) {
        return new ServiceArgument(name);
    }

    public static ArgumentType<Platform> Platform(String name) {
        return new PlatformArgument(name);
    }

    public static ArgumentType<CloudPlayer> Player(String name) {
        return new CloudPlayerArgument(name);
    }

    public static ArgumentType<ClusterNode> ClusterNode(String name) {
        return new ClusterNodeArgument(name);
    }

    public ArgumentType<T> asOptionalArgument() {
        return new Optional<>(this);
    }

    public static class Optional<T> extends ArgumentType<T> {

        private final ArgumentType<T> argument;

        public Optional(ArgumentType<T> argument) {
            super(argument.name(), false);
            this.argument = argument;
        }

        @Override
        public ParseResult<T> parse(String input) {
            return argument.parse(input);
        }
    }

    public record ParseResult<T>(T value, CommandError error) {

        public static <T> ParseResult<T> success(T value) {
            return new ParseResult<>(value, null);
        }

        public static <T> ParseResult<T> error(String message) {
            return new ParseResult<>(null, new CommandError(message));
        }

        public boolean isSuccess() {
            return error == null;
        }
    }

}
