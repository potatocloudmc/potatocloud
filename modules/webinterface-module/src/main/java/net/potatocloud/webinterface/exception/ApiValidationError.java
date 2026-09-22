package net.potatocloud.webinterface.exception;

public class ApiValidationError {

    public String field;
    public String message;

    public ApiValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }

}
