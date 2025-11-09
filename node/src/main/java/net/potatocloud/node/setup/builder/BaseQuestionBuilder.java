package net.potatocloud.node.setup.builder;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.potatocloud.node.setup.*;
import net.potatocloud.node.setup.validator.AnswerValidator;

@RequiredArgsConstructor
@Setter
@Accessors(fluent = true, chain = true)
public abstract class BaseQuestionBuilder {

    protected final Setup parent;
    protected final String name;
    protected final String prompt;

    private String defaultAnswer;
    private SuggestionProvider suggestions;
    private QuestionSkipCondition skipIf;
    private AnswerValidator customValidator;
    private AnswerAction answerAction;

    public abstract Question question();

    public void add() {
        final Question question = question();

        if (defaultAnswer != null) {
            question.setDefaultAnswer(defaultAnswer);
        }

        if (suggestions != null) {
            question.setSuggestions(suggestions);
        }

        if (skipIf != null) {
            question.setSkipCondition(skipIf);
        }

        if (customValidator != null) {
            question.setCustomValidator(customValidator);
        }

        if (answerAction != null) {
            question.setAnswerAction(answerAction);
        }

        parent.getQuestions().add(question);
    }
}
