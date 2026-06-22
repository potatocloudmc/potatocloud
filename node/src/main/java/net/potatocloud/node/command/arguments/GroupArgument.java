package net.potatocloud.node.command.arguments;

import net.potatocloud.api.group.Group;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.ArgumentType;

import java.util.List;

public class GroupArgument extends ArgumentType<Group> {

    public GroupArgument(String name) {
        super(name);
    }

    @Override
    public ParseResult<Group> parse(String input) {
        return Node.instance()
                .groupManager()
                .find(input)
                .map(ParseResult::success)
                .orElseGet(() -> ParseResult.error("Group &a" + input + " &7does &cnot &7exist"));
    }

    @Override
    public List<String> suggest(String input) {
        return Node.instance()
                .groupManager()
                .groups()
                .stream()
                .map(Group::name)
                .filter(name -> name.startsWith(input))
                .toList();
    }
}
