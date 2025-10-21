package net.potatocloud.node.command;

import lombok.Getter;
import net.potatocloud.node.Node;

@Getter
public abstract class SubCommand {

    private final String name;
    private final String description;
    private final String usage;

    protected SubCommand() {
        final SubCommandInfo info = this.getClass().getAnnotation(SubCommandInfo.class);
        if (info == null) {
            throw new IllegalStateException("SubCommandInfo annotation missing in SubCommand: " + getClass().getSimpleName());
        }
        name = info.name();
        description = info.description();
        usage = info.usage();
    }

    public abstract void execute(String[] args);

    protected void sendUsage() {
        Node.getInstance().getLogger().info("&cUse&8: &7" + usage);
    }
}
