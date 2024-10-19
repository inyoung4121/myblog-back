package in.myblog.post.repository;

import in.myblog.post.domain.Posts;
import in.myblog.post.dto.PostSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Posts, Long>{
    Page<Posts> findAll(Pageable pageable);


    @Query("SELECT p FROM Posts p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH p.comments c " +
            "LEFT JOIN FETCH c.author ca " +
            "WHERE p.id = :postId")
    Optional<Posts> findByIdWithAuthorAndComments(@Param("postId") Long postId);


    @Query("SELECT DISTINCT p FROM Posts p " +
            "LEFT JOIN FETCH p.postTags pt " +
            "LEFT JOIN FETCH pt.tag " +
            "WHERE p IN :posts")
    List<Posts> findPostsWithTags(List<Posts> posts);

    @Query(value = "SELECT p FROM Posts p",
            countQuery = "SELECT COUNT(p) FROM Posts p")
    Page<Posts> findAllPosts(Pageable pageable);

    @Query("SELECT p.id, COUNT(l) FROM Posts p LEFT JOIN p.likes l WHERE p IN :posts GROUP BY p.id")
    List<Object[]> countLikesForPosts(List<Posts> posts);

    @Query("SELECT DISTINCT p FROM Posts p JOIN p.postTags pt JOIN pt.tag t WHERE t.name IN :tagNames")
    Page<Posts> findByTagsIn(List<String> tagNames, Pageable pageable);
}
