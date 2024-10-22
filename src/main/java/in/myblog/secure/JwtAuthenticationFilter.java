package in.myblog.secure;

import in.myblog.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 인증이 필요한 경로들만 명시
        boolean requiresAuth =
                path.startsWith("/api/secure/") ||  // 보안이 필요한 API
                        (path.startsWith("/api/posts/") &&
                                (method.equals("POST") || method.equals("PUT") || method.equals("DELETE"))); // 글 작성/수정/삭제

        // true를 리턴하면 필터를 건너뛰므로, requiresAuth의 반대를 리턴
        return !requiresAuth;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String accessToken = jwtUtil.extractTokenFromHeader(request);
        String refreshToken = jwtUtil.extractTokenFromCookie(request);
        Long userId = null;

        if (accessToken != null) {
            userId = processAccessToken(accessToken, refreshToken, response);
        }

        if (userId == null && refreshToken != null) {
            userId = processRefreshToken(refreshToken, response);
        }

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateUser(userId, accessToken, request);
        }

        chain.doFilter(request, response);
    }

    private Long processAccessToken(String accessToken, String refreshToken, HttpServletResponse response) {
        try {
            if (jwtUtil.validateToken(accessToken)) {
                if (jwtUtil.isTokenExpired(accessToken)) {
                    log.info("Access token has expired. Attempting to use refresh token.");
                    return refreshTokens(refreshToken, response);
                } else {
                    return jwtUtil.getUserIdFromToken(accessToken);
                }
            } else {
                log.error("Invalid access token.");
            }
        } catch (Exception e) {
            log.error("Error processing access token: {}", e.getMessage());
        }
        return null;
    }

    private Long processRefreshToken(String refreshToken, HttpServletResponse response) {
        try {
            if (jwtUtil.validateToken(refreshToken) && !jwtUtil.isTokenExpired(refreshToken)) {
                return refreshTokens(refreshToken, response);
            } else {
                log.error("Refresh token is invalid or expired.");
            }
        } catch (Exception e) {
            log.error("Error processing refresh token: {}", e.getMessage());
        }
        return null;
    }

    private Long refreshTokens(String refreshToken, HttpServletResponse response) {
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId);
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        addRefreshTokenToCookie(response, newRefreshToken);
        return userId;
    }

    private void authenticateUser(Long userId, String accessToken, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUserId(userId);
        if (jwtUtil.validateToken(accessToken)) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("my_blog_refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }
}