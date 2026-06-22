package net.potatocloud.node.command.arguments;

import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.ArgumentType;

import java.util.List;

public class CloudPlayerArgument extends ArgumentType<CloudPlayer> {

    public CloudPlayerArgument(String name) {
        super(name);
    }

    @Override
    public ParseResult<CloudPlayer> parse(String input) {
        // input is the username of the player in this case
        return Node.instance()
                .playerManager()
                .find(input)
                .map(ParseResult::success)
                .orElseGet(() -> ParseResult.error("Player &a" + input + " &7was &cnot &7found"));
    }

    @Override
    public List<String> suggest(String input) {
        return Node.instance()
                .playerManager()
                .players()
                .stream()
                .map(CloudPlayer::username)
                .filter(name -> name.startsWith(input))
                .toList();
    }
}
