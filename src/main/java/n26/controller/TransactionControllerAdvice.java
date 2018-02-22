package n26.controller;

import n26.model.InvalidTimestampException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TransactionControllerAdvice {

    @ExceptionHandler(InvalidTimestampException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String handleNotFoundException(InvalidTimestampException ex) {
        return ex.getMessage();
    }

}
