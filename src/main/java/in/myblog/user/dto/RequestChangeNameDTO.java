package in.myblog.user.dto;

import lombok.Data;

@Data
public class RequestChangeNameDTO {
    String newUserName;
    String email;
}
