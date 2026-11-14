package net.potatocloud.plugins.optional.npc.action;

public record NpcAction(ActionType type, String text) {

    public NpcAction {
        final boolean hasText = text != null && !text.isBlank();
        if (type.needsText() != hasText) {
            throw new IllegalArgumentException(type + (type.needsText() ? " needs a text" : " has no text"));
        }
    }

    public static NpcAction of(ActionType type) {
        return new NpcAction(type, null);
    }
}
