package org.badmintonchain.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler  {

    @ExceptionHandler(value = UsernameNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleUsernameNotFoundException(UsernameNotFoundException ex){
        Map<String,Object> body = new HashMap<>();
        body.put("error","Username not found");
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler({
            VerificationTokenException.class,
            CourtException.class,
            BookingException.class
    })
    public ResponseEntity<Map<String,Object>> handleVerificationTokenException(RuntimeException ex, HttpServletRequest request){
        Map<String,Object> body = new HashMap<>();
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("timestamp", Instant.now());
        return ResponseEntity.badRequest().body(body);
    }



}
