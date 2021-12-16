package at.rtr.rmbt.exception;

import java.util.List;
import java.util.stream.Collectors;

public class InvalidFieldsException extends RuntimeException {

    public InvalidFieldsException(List<String> invalidElements) {
        super(invalidElements.stream().collect(Collectors.joining(", ", "invalid_fields : ", "")));
    }
}
