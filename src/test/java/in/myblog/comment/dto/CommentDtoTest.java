package in.myblog.comment.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CommentDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCommentDtoDeserialization() throws Exception {
        String json = "{\"content\":\"1\",\"postId\":1,\"userId\":null,\"anonymous\":true,\"anonymousName\":\"1\",\"deletePassword\":\"1\"}";

        CommentDto commentDto = objectMapper.readValue(json, CommentDto.class);

        System.out.println("Deserialized DTO: " + commentDto);
        System.out.println("Is anonymous: " + commentDto.isAnonymous());

        assertTrue(commentDto.isAnonymous());
    }
}