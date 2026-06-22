package net.potatocloud.node.command.arguments;

import net.potatocloud.api.service.Service;
import net.potatocloud.node.Node;
import net.potatocloud.node.command.ArgumentType;

import java.util.List;

public class ServiceArgument extends ArgumentType<Service> {

    public ServiceArgument(String name) {
        super(name);
    }

    @Override
    public ParseResult<Service> parse(String input) {
        return Node.instance()
                .serviceManager()
                .find(input)
                .map(ParseResult::success)
                .orElseGet(() -> ParseResult.error("Service &a" + input + " &7does &cnot &7exist"));
    }

    @Override
    public List<String> suggest(String input) {
        return Node.instance()
                .serviceManager()
                .services()
                .stream()
                .map(Service::name)
                .filter(name -> name.startsWith(input))
                .toList();
    }
}
