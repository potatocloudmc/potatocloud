package net.potatocloud.node.command.commands;

import lombok.RequiredArgsConstructor;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;
import net.potatocloud.node.console.Console;

@RequiredArgsConstructor
@CommandInfo(name = "clear", description = "Clears console screen", aliases = {"cls"})
public class ClearCommand extends Command {

    private final Console console;

    @Override
    public void execute(String[] args) {
        console.clearScreen();
    }
}
