package net.potatocloud.node.service.start.rule;

import net.potatocloud.api.group.Group;

public final class MaintenanceRule implements ServiceStartRule {

    @Override
    public boolean allows(Group group) {
        return !group.maintenance();
    }
}
