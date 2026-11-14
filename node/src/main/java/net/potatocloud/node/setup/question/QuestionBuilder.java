package net.potatocloud.node.setup.question;

import net.potatocloud.node.setup.answer.AnswerAction;
import net.potatocloud.node.setup.answer.Validator;

import java.util.List;

public class QuestionBuilder {

    private final List<Question> questions;
    private final String name;
    private final String prompt;
    private final QuestionType type;

    private String defaultAnswer;
    private Suggestions suggestions;
    private SkipCondition skipCondition;
    private Validator customValidator;
    private AnswerAction answerAction;

    public QuestionBuilder(List<Question> questions, String name, String prompt, QuestionType type) {
        this.questions = questions;
        this.name = name;
        this.prompt = prompt;
        this.type = type;
    }

    public QuestionBuilder defaultAnswer(String defaultAnswer) {
        this.defaultAnswer = defaultAnswer;
        return this;
    }

    public QuestionBuilder suggestions(Suggestions suggestions) {
        this.suggestions = suggestions;
        return this;
    }

    public QuestionBuilder skipIf(SkipCondition skipCondition) {
        this.skipCondition = skipCondition;
        return this;
    }

    public QuestionBuilder customValidator(Validator customValidator) {
        this.customValidator = customValidator;
        return this;
    }

    public QuestionBuilder answerAction(AnswerAction answerAction) {
        this.answerAction = answerAction;
        return this;
    }

    public void add() {
        questions.add(new Question(name, prompt, type, defaultAnswer, suggestions, skipCondition, customValidator, answerAction));
    }
}