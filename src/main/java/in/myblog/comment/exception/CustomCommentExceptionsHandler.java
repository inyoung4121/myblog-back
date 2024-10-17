package in.myblog.comment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomCommentExceptionsHandler {

    @ExceptionHandler(CustomCommentExceptions.CommentNotFoundException.class)
    public ResponseEntity<String> handleCommentNotFoundException(CustomCommentExceptions.CommentNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);  // 404 Not Found
    }

    @ExceptionHandler(CustomCommentExceptions.CommentAccessDeniedException.class)
    public ResponseEntity<String> handleCommentAccessDeniedException(CustomCommentExceptions.CommentAccessDeniedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);  // 403 Forbidden
    }

    @ExceptionHandler(CustomCommentExceptions.CommentAccessDeniedException.class)
    public ResponseEntity<String> handleCommentAccessDeniedException(CustomCommentExceptions.InvalidPasswordException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);  // 403 Forbidden
    }
}