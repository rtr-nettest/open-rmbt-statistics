package at.rtr.rmbt.advice;

import at.rtr.rmbt.exception.InvalidFieldsException;
import at.rtr.rmbt.exception.InvalidImageTypeException;
import at.rtr.rmbt.response.ErrorResponse;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ControllerErrorAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        return new ErrorResponse(
                ex.getBindingResult().getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(", "))
        );
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnrecognizedPropertyException(UnrecognizedPropertyException ex) {
        log.error(ex.getLocalizedMessage());

        final String error = "JSON parse error: Unrecognized field " + "[ " + ex.getPropertyName() + " ]";

        return new ErrorResponse(error);
    }

    @ExceptionHandler(InvalidFieldsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidFieldsException(InvalidFieldsException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(value = {InvalidFieldsException.class,
            InvalidImageTypeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidLanguageException(RuntimeException e) {
        return e.getMessage();
    }
}
