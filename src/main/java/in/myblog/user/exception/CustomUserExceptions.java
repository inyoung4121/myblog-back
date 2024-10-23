package in.myblog.user.exception;


public class CustomUserExceptions {
    private CustomUserExceptions() {}

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException() {
            super("해당 사용자를 찾을 수 없습니다");
        }
    }

    public static class DuplicateUsernameException extends RuntimeException {
        public DuplicateUsernameException() {
            super("이미 사용중인 이름입니다");
        }
    }

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException() {
            super("이미 사용중인 이메일입니다");
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("비밀번호 또는 이메일이 틀렸습니다");
        }
    }

    public static class RoleChangeRequestNotFoundException extends RuntimeException {
        public RoleChangeRequestNotFoundException() {
            super("역할 변경 요청을 찾을 수 없습니다.");
        }
    }

    public static class InvalidRoleChangeRequestStatusException extends RuntimeException {
        public InvalidRoleChangeRequestStatusException() {
            super("이미 처리된 요청입니다");
        }
    }

    public static class DuplicateRoleChangeRequestException extends RuntimeException {
        public DuplicateRoleChangeRequestException() {
            super("이미 처리 중인 권한 변경 요청이 있습니다.");
        }
    }
}
