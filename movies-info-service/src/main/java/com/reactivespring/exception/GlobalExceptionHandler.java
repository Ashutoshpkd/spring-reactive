package com.reactivespring.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, String>> handleRequestBodyError(WebExchangeBindException ex) {
        log.error("Exception caught in handleRequestBodyError - {} ", ex.getMessage());
        Map<String, String> errors = new HashMap<>();

        for (ObjectError error : ex.getAllErrors()) {
            errors.put(((FieldError) error).getField(), error.getDefaultMessage());
        }
        log.error("Error is - {} ", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
