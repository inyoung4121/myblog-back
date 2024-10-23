package in.myblog.secure;

import in.myblog.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            String accessToken = jwtUtil.extractTokenFromHeader(request);
            String refreshToken = jwtUtil.extractTokenFromCookie(request);

            // 1. Access Token이 있는 경우
            if (accessToken != null) {
                try {
                    // Access Token 검증 시도
                    if (jwtUtil.validateToken(accessToken)) {
                        authenticateUser(jwtUtil.getUserIdFromToken(accessToken), request);
                        chain.doFilter(request, response);
                        return;
                    }
                } catch (ExpiredJwtException e) {
                    // Access Token이 만료된 경우, Refresh Token으로 재발급 시도
                    log.info("Access token expired. Attempting token refresh...");
                    handleTokenRefresh(refreshToken, response, request, chain);
                    return;
                } catch (Exception e) {
                    log.error("Access token validation failed", e);
                }
            }

            // 2. Access Token이 없고 Refresh Token만 있는 경우
            if (refreshToken != null) {
                handleTokenRefresh(refreshToken, response, request, chain);
                return;
            }

            // 3. 둘 다 없는 경우 - 인증 실패
            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Authentication process failed", e);
            handleAuthenticationFailure(response);
        }
    }

    private void handleTokenRefresh(String refreshToken, HttpServletResponse response,
                                    HttpServletRequest request, FilterChain chain)
            throws IOException, ServletException {
        try {
            // Refresh Token 유효성 검증
            if (!jwtUtil.validateToken(refreshToken)) {
                handleAuthenticationFailure(response);
                return;
            }

            // Refresh Token에서 userId 추출
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);

            // 새로운 Access Token 발급
            String newAccessToken = jwtUtil.generateAccessToken(userId);

            // Refresh Token 만료 시간이 얼마 남지 않은 경우 새로 발급
            if (jwtUtil.shouldRefreshToken(refreshToken)) {
                String newRefreshToken = jwtUtil.generateRefreshToken(userId);
                addRefreshTokenToCookie(response, newRefreshToken);
            }

            // 새로운 Access Token을 응답 헤더에 추가
            response.setHeader("Authorization", "Bearer " + newAccessToken);

            // 사용자 인증 처리
            authenticateUser(userId, request);

            // 필터 체인 계속 진행
            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            handleAuthenticationFailure(response);
        }
    }

    private void authenticateUser(Long userId, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUserId(userId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleAuthenticationFailure(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"Authentication failed\"}");
    }

    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("my_blog_refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);
    }
}