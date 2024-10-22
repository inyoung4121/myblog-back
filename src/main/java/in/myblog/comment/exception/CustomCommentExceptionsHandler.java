package in.myblog.comment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomCommentExceptionsHandler {

    @ExceptionHandler(CustomCommentExceptions.CommentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCommentNotFoundException(CustomCommentExceptions.CommentNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CustomCommentExceptions.CommentAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleCommentAccessDeniedException(CustomCommentExceptions.CommentAccessDeniedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(CustomCommentExceptions.InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPasswordException(CustomCommentExceptions.InvalidPasswordException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}