package in.myblog.post.repository;

import in.myblog.post.domain.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Posts, Long>{
    @Query(value = "SELECT p.id as id, p.title as title, u.username as authorName, " +
            "p.created_at as createdAt, " +
            "CASE WHEN LENGTH(p.content) > 100 THEN CONCAT(SUBSTRING(p.content, 1, 100), '...') " +
            "ELSE p.content END as contentPreview " +
            "FROM posts p JOIN users u ON p.author_id = u.id " +
            "ORDER BY p.id DESC",
            countQuery = "SELECT COUNT(*) FROM posts",
            nativeQuery = true)
    Page<PostSummary> findAllPostSummaries(Pageable pageable);
}
