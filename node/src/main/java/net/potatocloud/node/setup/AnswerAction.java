package net.potatocloud.node.setup;

import java.util.Map;

public interface AnswerAction {

    void execute(Map<String, String> answers, String answer);

}
