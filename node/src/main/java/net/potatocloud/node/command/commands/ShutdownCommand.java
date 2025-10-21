package net.potatocloud.node.command.commands;

import lombok.RequiredArgsConstructor;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.Command;
import net.potatocloud.node.command.CommandInfo;

@RequiredArgsConstructor
@CommandInfo(name = "shutdown", description = "Shutdown the node", aliases = {"stop", "end"})
public class ShutdownCommand extends Command {

    private final Node node;

    @Override
    public void execute(String[] args) {
        node.shutdown();
    }
}
