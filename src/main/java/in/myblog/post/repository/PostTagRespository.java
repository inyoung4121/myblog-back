package in.myblog.post.repository;

import in.myblog.post.domain.PostTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTagRespository extends JpaRepository<PostTags, Long> {
    void deleteByPostId(Long postId);
}
