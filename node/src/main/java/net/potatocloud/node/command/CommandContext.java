package net.potatocloud.node.command;

import java.util.HashMap;
import java.util.Map;

public class CommandContext {

    private final Map<String, Object> values = new HashMap<>();

    private CommandError error;

    public Map<String, Object> values() {
        return values;
    }

    public <T> void set(String name, T value) {
        values.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) values.get(name);
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    public boolean isSuccess() {
        return error == null;
    }

    public record ParseResult(CommandContext context, int parsedArguments, boolean complete, String errorMessage) {

        public ParseResult(CommandContext context, int parsedArguments, boolean complete) {
            this(context, parsedArguments, complete, null);
        }
    }
}
