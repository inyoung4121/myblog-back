package in.myblog.user.dto;

import in.myblog.user.domain.Users;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Users}
 */
@Value
public class RequestLoginDTO implements Serializable {
    String password;
    String email;
}