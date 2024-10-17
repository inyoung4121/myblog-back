package in.myblog.comment.exception;

public class CustomCommentExceptions {
    private CustomCommentExceptions() {
        throw new AssertionError("This class should not be instantiated.");
    }

    public static class CommentNotFoundException extends RuntimeException {
        public CommentNotFoundException(Long commentId) {
            super(String.format("댓글 ID %d를 찾을 수 없습니다", commentId));
        }
    }

    public static class CommentAccessDeniedException extends RuntimeException {
        public CommentAccessDeniedException() {
            super("해당 댓글에 권한이 없습니다");
        }
    }

    public static class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException() {
            super("비밀번호가 틀렸습니다.");
        }
    }
}
