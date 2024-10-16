package in.myblog.user.dto;

import in.myblog.user.domain.Users;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseUserDTO {
    private Long id;
    private String username;
    private String email;

    public ResponseUserDTO(Users user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }
}
