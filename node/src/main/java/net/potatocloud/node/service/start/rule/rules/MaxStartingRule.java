package net.potatocloud.node.service.start.rule.rules;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.node.service.start.rule.ServiceStartRule;

@RequiredArgsConstructor
public class MaxStartingRule implements ServiceStartRule {

    @Override
    public boolean allows(ServiceGroup group) {
        return true;
    }
}
