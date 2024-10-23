package in.myblog.like.repository;

import in.myblog.like.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like,Long> {
    Optional<Like> findByPostIdAndDeviceId(Long postId, String deviceId);
    Long countByPostId(Long postId);
    Boolean existsByPostIdAndDeviceId(Long postId, String deviceId);
    @Modifying
    @Query("delete from Like l where l.post.id = :postId")
    void deleteAllByPostIdInBatch(@Param("postId") Long postId);
    void deleteByPostId(Long postId);
}
