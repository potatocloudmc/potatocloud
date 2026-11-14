package net.potatocloud.node.command;

import net.potatocloud.api.logging.Logger;

import java.util.*;

public class CommandManager {

    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, Command> aliases = new HashMap<>();

    private Logger logger;

    public Map<String, Command> commands() {
        return commands;
    }

    public void logger(Logger logger) {
        this.logger = logger;
    }

    public void registerCommand(Command command) {
        commands.put(command.name().toLowerCase(), command);
        command.aliases().forEach(alias -> aliases.put(alias.toLowerCase(), command));
    }

    public void executeCommand(String line) {
        if (line.isBlank()) {
            return;
        }

        final String[] parts = line.trim().split(" ");
        final String input = parts[0].toLowerCase();

        Command command = commands.get(input);
        if (command == null) {
            command = aliases.get(input);
        }

        if (command == null) {
            logger.info("Command &a" + input + " &7does &cnot &7exist");
            return;
        }

        final String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        command.execute(args);
    }

    public Command getCommand(String name) {
        final Command command = commands.get(name.toLowerCase());
        if (command != null) {
            return command;
        }
        return aliases.get(name.toLowerCase());
    }

    public List<String> getAllCommandNames() {
        final List<String> names = new ArrayList<>(commands.keySet());
        aliases.keySet().stream().filter(alias -> !names.contains(alias)).forEach(names::add);
        return names;
    }
}