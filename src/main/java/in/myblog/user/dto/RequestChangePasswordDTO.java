package in.myblog.user.dto;

import lombok.Data;

@Data
public class RequestChangePasswordDTO {
    String oldPassword;
    String newPassword;
    String email;
}
