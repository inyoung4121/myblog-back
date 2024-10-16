package in.myblog.user.exception;


public class CustomUserExceptions {
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class DuplicateUsernameException extends RuntimeException {
        public DuplicateUsernameException(String message) {
            super(message);
        }
    }

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) {
            super(message);
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public static class RoleChangeRequestNotFoundException extends RuntimeException {
        public RoleChangeRequestNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidRoleChangeRequestStatusException extends RuntimeException {
        public InvalidRoleChangeRequestStatusException(String message) {
            super(message);
        }
    }
}
