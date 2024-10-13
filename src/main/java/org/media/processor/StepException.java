package org.media.processor;

public class StepException extends RuntimeException {
    public StepException(String message) {
        this(message, null);
    }

    public StepException(String message, Throwable cause) {
        super(message, cause);
    }
}
