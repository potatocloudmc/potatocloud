package net.potatocloud.node.setup;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SetupQuestion {

    private final String name;
    private final String question;
    private final String defaultAnswer;
    private final SetupChoicesProvider choicesProvider;
    private final SetupAnswerValidator validator;
    private final SetupQuestionSkipCondition skipCondition;

    public boolean shouldSkip(Map<String, String> answers) {
        if (skipCondition == null) {
            return false;
        }
        return skipCondition.skip(answers);
    }

    public List<String> getPossibleChoices(Map<String, String> answers) {
        if (choicesProvider == null) {
            return List.of();
        }
        return choicesProvider.getChoices(answers);
    }
}
