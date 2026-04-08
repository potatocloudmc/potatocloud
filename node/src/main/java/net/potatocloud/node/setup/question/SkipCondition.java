package net.potatocloud.node.setup.question;

import java.util.Map;

public interface SkipCondition {

    boolean skip(Map<String, String> answers);

}
