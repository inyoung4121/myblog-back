package in.myblog.post.repository;

import in.myblog.like.domain.Like;
import in.myblog.post.domain.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Posts, Long>{
    Page<Posts> findAll(Pageable pageable);

    @Query("SELECT EXISTS (SELECT 1 FROM Posts p WHERE p.id = :postId)")
    boolean existsById(Long postId);

    @Query("SELECT l FROM Like l WHERE l.post.id = :postId AND l.deviceId = :deviceId")
    Optional<Like> findLikeByPostIdAndDeviceId(Long postId, String deviceId);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId")
    long countLikesByPostId(Long postId);
}
