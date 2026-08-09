package net.potatocloud.node.service.start.condition;

import net.potatocloud.api.group.Group;

public interface ServiceStartCondition {

    boolean shouldStart(Group group);

}
