package net.potatocloud.node.setup;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetupQuestionBuilder {

    private final String name;
    private String question;
    private String defaultAnswer;
    private SetupChoicesProvider choicesProvider;
    private SetupAnswerValidator validator;
    private SetupQuestionSkipCondition skipCondition;

    private final Setup parent;

    public SetupQuestionBuilder(String name, Setup parent) {
        this.name = name;
        this.parent = parent;
    }

    public SetupQuestionBuilder question(String question) {
        this.question = question;
        return this;
    }

    public SetupQuestionBuilder defaultAnswer(String defaultAnswer) {
        this.defaultAnswer = defaultAnswer;
        return this;
    }

    public SetupQuestionBuilder choices(SetupChoicesProvider choicesProvider) {
        this.choicesProvider = choicesProvider;
        return this;
    }

    public SetupQuestionBuilder validator(SetupAnswerValidator validator) {
        this.validator = validator;
        return this;
    }

    public SetupQuestionBuilder skipCondition(SetupQuestionSkipCondition skipCondition) {
        this.skipCondition = skipCondition;
        return this;
    }

    public void done() {
        final SetupQuestion question = new SetupQuestion(
                name,
                this.question,
                defaultAnswer,
                choicesProvider,
                validator,
                skipCondition
        );
        parent.getQuestions().add(question);
    }
}
