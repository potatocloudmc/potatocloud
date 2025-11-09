package net.potatocloud.node.setup;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.potatocloud.node.setup.validator.AnswerValidator;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class AbstractQuestion implements Question {

    private final String name;
    private final String prompt;
    private final QuestionType type;
    private QuestionSkipCondition skipCondition;
    private String defaultAnswer;
    private SuggestionProvider suggestions;
    private AnswerValidator customValidator;
    private AnswerAction answerAction;

    @Override
    public List<String> getSuggestions() {
        if (suggestions == null) {
            return null;
        }
        return suggestions.suggest();
    }

    @Override
    public boolean shouldSkip(Map<String, String> answers) {
        if (skipCondition == null) {
            return false;
        }
        return skipCondition.skip(answers);
    }

    @Override
    public boolean validateInput(String input) {
        final AnswerValidator defaultValidator = getDefaultValidator();
        if (defaultValidator != null) {
            final AnswerResult result = defaultValidator.validateInput(input);

            if (!result.isSuccess()) {
                return false;
            }
        }

        if (customValidator != null) {
            final AnswerResult result = customValidator.validateInput(input);
            if (!result.isSuccess()) {
                return false;
            }
        }

        return true;
    }


    @Override
    public String getValidatorError(String input) {
        final AnswerValidator defaultValidator = getDefaultValidator();
        if (defaultValidator != null) {
            final AnswerResult result = defaultValidator.validateInput(input);
            if (!result.isSuccess()) {
                return result.getErrorMessage();
            }
        }

        if (customValidator != null) {
            final AnswerResult result = customValidator.validateInput(input);
            if (!result.isSuccess()) {
                return result.getErrorMessage();
            }
        }

        return "Error";
    }

}


