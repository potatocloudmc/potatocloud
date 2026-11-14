package net.potatocloud.node.setup.question;

import net.potatocloud.node.setup.answer.AnswerAction;
import net.potatocloud.node.setup.answer.AnswerResult;
import net.potatocloud.node.setup.answer.Validator;

import java.util.List;
import java.util.Map;

public record Question(
        String name,
        String prompt,
        QuestionType type,
        String defaultAnswer,
        Suggestions suggestions,
        SkipCondition skipCondition,
        Validator customValidator,
        AnswerAction answerAction
) {

    public List<String> getSuggestions() {
        if (type == QuestionType.BOOLEAN) {
            return List.of("true", "false", "yes", "no");
        }
        return suggestions == null ? null : suggestions.suggest();
    }

    public boolean shouldSkip(Map<String, String> answers) {
        return skipCondition != null && skipCondition.skip(answers);
    }

    public AnswerResult validate(String input) {
        switch (type) {
            case TEXT -> {
                if (input.isBlank()) {
                    return AnswerResult.error("The input cannot be blank");
                }
            }
            case NUMBER -> {
                try {
                    Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    return AnswerResult.error("Please enter a valid number");
                }
            }
            case BOOLEAN -> {
                final String lower = input.toLowerCase();
                if (!lower.equals("true") && !lower.equals("false") && !lower.equals("yes") && !lower.equals("no")) {
                    return AnswerResult.error("Please enter yes/true or no/false");
                }
            }
        }

        if (customValidator != null) {
            return customValidator.validate(input);
        }

        return AnswerResult.success();
    }
}
