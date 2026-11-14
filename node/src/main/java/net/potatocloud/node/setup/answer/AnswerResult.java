package net.potatocloud.node.setup.answer;

public class AnswerResult {

    private final boolean success;
    private final String errorMessage;

    private AnswerResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public static AnswerResult success() {
        return new AnswerResult(true, null);
    }

    public static AnswerResult error(String message) {
        return new AnswerResult(false, message);
    }
}
