package in.myblog.comment.repository;

import in.myblog.comment.domain.Comments;
import in.myblog.post.domain.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Long> {
    List<Comments> findByPostOrderByCreatedAtDesc(Posts post);
}
