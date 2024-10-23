package in.myblog.post.exception;

public class CustomPostExceptions {

    private CustomPostExceptions() {
        throw new AssertionError("This class should not be instantiated.");
    }

    public static class PostNotFoundException extends RuntimeException {
        public PostNotFoundException(Long postId) {
            super(String.format("게시물 ID %d를 찾을 수 없습니다", postId));
        }
    }

    public static class UserMissMatchException extends RuntimeException {
        public UserMissMatchException(Long postId) {
            super(String.format("게시물 ID %d에 대한 권한이 없습니다", postId));
        }
    }
    public static class PostDeleteFailedException extends RuntimeException {
        public PostDeleteFailedException() {
            super(String.format("게시물 삭제에 실패했습니다"));
        }
    }
}
