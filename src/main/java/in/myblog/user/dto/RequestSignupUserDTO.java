package in.myblog.user.dto;

import in.myblog.user.domain.Users;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Users}
 */
@Value
public class RequestSignupUserDTO implements Serializable {
    String username;
    String password;
    String email;
}