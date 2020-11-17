package com.antra.evaluation.reporting_system.exceptionHandler;

import com.antra.evaluation.reporting_system.exception.FileGenerationException;
import com.antra.evaluation.reporting_system.pojo.api.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileGenerationException.class)
    public ResponseEntity<ErrorResponse> exceptionHandlerReportNotFound(Exception ex) {
        ErrorResponse error = new ErrorResponse("Generation failed", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(Exception ex) {
        ErrorResponse error = new ErrorResponse("Request failed", HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
