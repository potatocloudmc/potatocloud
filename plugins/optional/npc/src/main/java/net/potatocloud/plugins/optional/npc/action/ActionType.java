package net.potatocloud.plugins.optional.npc.action;

public enum ActionType {

    NONE(false),
    SELECTOR(false),
    CONNECT(false),
    COMMAND(true),
    MESSAGE(true);

    private final boolean needsText;

    ActionType(boolean needsText) {
        this.needsText = needsText;
    }

    public boolean needsText() {
        return needsText;
    }
}
