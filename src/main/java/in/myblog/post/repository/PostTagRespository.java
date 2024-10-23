package in.myblog.post.repository;

import in.myblog.post.domain.PostTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRespository extends JpaRepository<PostTags, Long> {
    void deleteByPostId(Long postId);
    @Modifying
    @Query("delete from PostTags pt where pt.post.id = :postId")
    void deleteAllByPostIdInBatch(@Param("postId") Long postId);

    @Query("SELECT pt FROM PostTags pt JOIN FETCH pt.tag WHERE pt.post.id = :postId")
    List<PostTags> findByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(pt) FROM PostTags pt WHERE pt.tag.id = :tagId")
    long countByTagId(@Param("tagId") Long tagId);
}
