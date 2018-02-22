package n26.controller;

import n26.model.NoStatisticsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class StatisticsControllerAdvice {

    @ExceptionHandler(NoStatisticsException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String handleNoStatisticsException(NoStatisticsException ex) {
        return ex.getMessage();
    }

}
