package net.potatocloud.node.setup;

import net.potatocloud.node.setup.validator.AnswerValidator;

import java.util.List;
import java.util.Map;

public interface Question {

    String getName();

    String getPrompt();

    boolean shouldSkip(Map<String, String> answers);

    void setSkipCondition(QuestionSkipCondition skipCondition);

    boolean validateInput(String input);

    String getValidatorError(String input);

    String getDefaultAnswer();

    void setDefaultAnswer(String defaultAnswer);

    List<String> getSuggestions();

    void setSuggestions(SuggestionProvider provider);

    AnswerValidator getDefaultValidator();

    AnswerValidator getCustomValidator();

    void setCustomValidator(AnswerValidator validator);

    QuestionType getType();

    AnswerAction getAnswerAction();

    void setAnswerAction(AnswerAction action);

}
