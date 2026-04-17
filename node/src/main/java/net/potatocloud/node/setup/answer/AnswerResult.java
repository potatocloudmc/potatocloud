package net.potatocloud.node.setup.answer;

import lombok.Getter;

@Getter
public class AnswerResult {

    private final boolean success;
    private final String errorMessage;

    private AnswerResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static AnswerResult success() {
        return new AnswerResult(true, null);
    }

    public static AnswerResult error(String message) {
        return new AnswerResult(false, message);
    }
}
