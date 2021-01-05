package org.obiba.rock.domain;

import org.obiba.rock.model.ErrorMessage;
import org.springframework.http.HttpStatus;

public class ExceptionErrorMessage implements ErrorMessage {

    private final String status;

    private final Exception exception;

    private final String[] args;

    public ExceptionErrorMessage(String status, Exception exception, String... args) {
        this.status = status;
        this.exception = exception;
        this.args = args;
    }

    public ExceptionErrorMessage(HttpStatus status, Exception exception, String... args) {
        this.status = status.value() + "";
        this.exception = exception;
        this.args = args;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getKey() {
        return exception.getClass().getSimpleName();
    }

    @Override
    public String[] getArgs() {
        return args;
    }

    @Override
    public String getMessage() {
        return exception.getMessage();
    }
}
