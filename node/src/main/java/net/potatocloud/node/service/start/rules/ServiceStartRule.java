package net.potatocloud.node.service.start.rules;

import net.potatocloud.api.group.Group;

public interface ServiceStartRule {

    boolean allows(Group group);

}
