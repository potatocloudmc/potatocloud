package net.potatocloud.plugins.optional.npc.task;

import net.potatocloud.plugins.optional.npc.entity.NpcEntityService;
import net.potatocloud.plugins.optional.npc.selector.SelectorMenu;

public final class UpdateTask implements Runnable {

    private final NpcEntityService entityService;
    private final SelectorMenu selectorMenu;

    public UpdateTask(NpcEntityService entityService, SelectorMenu selectorMenu) {
        this.entityService = entityService;
        this.selectorMenu = selectorMenu;
    }

    @Override
    public void run() {
        entityService.updateHolograms();
        selectorMenu.refresh();
    }
}
