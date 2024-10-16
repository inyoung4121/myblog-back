package in.myblog.user.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomUserExceptionsHandler {

    @ExceptionHandler(CustomUserExceptions.UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(CustomUserExceptions.UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(CustomUserExceptions.DuplicateUsernameException.class)
    public ResponseEntity<String> handleDuplicateUsernameException(CustomUserExceptions.DuplicateUsernameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(CustomUserExceptions.DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmailException(CustomUserExceptions.DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(CustomUserExceptions.InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentialsException(CustomUserExceptions.InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(CustomUserExceptions.RoleChangeRequestNotFoundException.class)
    public ResponseEntity<String> handleRoleChangeRequestNotFoundException(CustomUserExceptions.RoleChangeRequestNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(CustomUserExceptions.InvalidRoleChangeRequestStatusException.class)
    public ResponseEntity<String> handleInvalidRoleChangeRequestStatusException(CustomUserExceptions.InvalidRoleChangeRequestStatusException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access to this resource is forbidden");
    }
}
