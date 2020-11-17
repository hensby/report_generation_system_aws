package com.antra.report.client.exceptionHandler;

import com.antra.report.client.exception.ReportNotFoundException;
import com.antra.report.client.exception.RequestNotFoundException;
import com.antra.report.client.pojo.reponse.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/*
    changed by Hengchao
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ErrorResponse> exceptionHandlerReportNotFound(ReportNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND, "Report not found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> exceptionHandlerRequestNotFound(RequestNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND, "Request not found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(Exception ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST, "Request failed");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}