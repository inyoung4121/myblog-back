package in.myblog.post.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomPostExceptionsHandler {
    @ExceptionHandler(CustomPostExceptions.PostNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRoleChangeRequestNotFoundException(CustomPostExceptions.PostNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CustomPostExceptions.UserMissMatchException.class)
    public ResponseEntity<Map<String, String>> handleRoleChangeRequestNotFoundException(CustomPostExceptions.UserMissMatchException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(CustomPostExceptions.PostDeleteFailedException.class)
    public ResponseEntity<Map<String, String>> handlePostDeleteFailedException(CustomPostExceptions.PostDeleteFailedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(CustomPostExceptions.ImageUploadFailedException.class)
    public ResponseEntity<Map<String, String>> handleImageUploadFailedException(CustomPostExceptions.ImageUploadFailedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}