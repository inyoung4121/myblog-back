package in.myblog.user.service;

import in.myblog.jwt.JwtUtil;
import in.myblog.user.domain.UserRole;
import in.myblog.user.domain.RoleChangeRequest;
import in.myblog.user.domain.Users;
import in.myblog.user.dto.ResponseUserDTO;
import in.myblog.user.exception.CustomUserExceptions;
import in.myblog.user.repository.RoleChangeRequestRepository;
import in.myblog.user.repository.UserRepository;
import in.myblog.user.exception.CustomUserExceptions.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleChangeRequestRepository roleChangeRequestRepository;
    private final JwtUtil jwtUtil;

    public ResponseUserDTO registerUser(String username, String email, String password, HttpServletResponse response) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUsernameException();
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException();
        }

        Users user = new Users().builder()
                .role(UserRole.USER)
                .email(email)
                .password(password)
                .username(username)
                .build();
        user = userRepository.save(user);

        return authenticateUser(user, response);
    }

    public ResponseUserDTO login(String email, String password, HttpServletResponse response) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return authenticateUser(user, response);
    }

    private ResponseUserDTO authenticateUser(Users user, HttpServletResponse response) {
        String newAccessToken = jwtUtil.generateAccessToken(user.getId());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        response.setHeader("Authorization", "Bearer " + newAccessToken);
        addRefreshTokenToCookie(response, newRefreshToken);

        return new ResponseUserDTO(user);
    }

    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("my_blog_refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Users user = userRepository.findById(userId)
                .orElseThrow(CustomUserExceptions.UserNotFoundException::new);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        user.changePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public Users changeUsername(Long userId, String newUsername) {
        Users user = userRepository.findById(userId)
                .orElseThrow(CustomUserExceptions.UserNotFoundException::new);

        userRepository.findByUsername(newUsername).ifPresent(u -> {
            throw new DuplicateUsernameException();
        });

        user.changeUsername(newUsername);
        return user;
    }

    @Transactional
    public RoleChangeRequest requestRoleChange(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(CustomUserExceptions.UserNotFoundException::new);

        RoleChangeRequest request = RoleChangeRequest.builder()
                .user(user)
                .status(RoleChangeRequest.RequestStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build();

        return roleChangeRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<RoleChangeRequest> getAllRoleChangeRequests(){
        return roleChangeRequestRepository.findAll();
    }

    @Transactional
    public void approveRoleChangeRequest(Long requestId) {
        RoleChangeRequest request = roleChangeRequestRepository.findById(requestId)
                .orElseThrow(RoleChangeRequestNotFoundException::new);

        if (request.getStatus() != RoleChangeRequest.RequestStatus.PENDING) {
            throw new InvalidRoleChangeRequestStatusException();
        }

        request.approve();
        request.getUser().changeRole(UserRole.MANAGER);
    }

    @Transactional
    public void rejectRoleChangeRequest(Long requestId) {
        RoleChangeRequest request = roleChangeRequestRepository.findById(requestId)
                .orElseThrow(RoleChangeRequestNotFoundException::new);

        if (request.getStatus() != RoleChangeRequest.RequestStatus.PENDING) {
            throw new InvalidRoleChangeRequestStatusException();
        }

        request.reject();
    }
}