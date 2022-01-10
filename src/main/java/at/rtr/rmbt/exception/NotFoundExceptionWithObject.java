package at.rtr.rmbt.exception;

import lombok.Getter;

@Getter
public class NotFoundExceptionWithObject extends RuntimeException {

    private final Object responseObject;

    public NotFoundExceptionWithObject(Object responseObject) {
        this.responseObject = responseObject;
    }
}
