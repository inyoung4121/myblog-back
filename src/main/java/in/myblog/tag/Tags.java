package in.myblog.tag;

import in.myblog.post.PostTags;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostTags> postTags = new ArrayList<>();
}