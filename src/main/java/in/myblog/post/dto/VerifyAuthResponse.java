package in.myblog.post.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyAuthResponse {
    private boolean isAuthenticated;
    private String role;
}