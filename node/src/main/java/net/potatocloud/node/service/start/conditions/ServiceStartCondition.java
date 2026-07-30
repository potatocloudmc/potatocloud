package net.potatocloud.node.service.start.conditions;

import net.potatocloud.api.group.Group;

public interface ServiceStartCondition {

    boolean shouldStart(Group group);

}
