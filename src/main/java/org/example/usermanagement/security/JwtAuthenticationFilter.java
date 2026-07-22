package org.example.usermanagement.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter
                extends OncePerRequestFilter {

        public static final String JWT_ERROR_ATTRIBUTE = "jwtError";

        public static final String ACCOUNT_FORBIDDEN_ATTRIBUTE = "accountForbidden";

        private final JwtUtils jwtUtils;

        private final UserDetailsService userDetailsService;

        private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

        @Override
        protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain) throws ServletException, IOException {

                String token = resolveToken(request);

                if (!StringUtils.hasText(token)) {
                        filterChain.doFilter(
                                        request,
                                        response);

                        return;
                }

                try {
                        String email = jwtUtils.extractUsername(
                                        token);

                        boolean notAuthenticated = SecurityContextHolder
                                        .getContext()
                                        .getAuthentication() == null;

                        if (StringUtils.hasText(email)
                                        && notAuthenticated) {
                                /*
                                 * Tải lại tài khoản từ database ở mỗi request.
                                 * Vì vậy trạng thái LOCKED mới nhất luôn được kiểm tra.
                                 */
                                UserDetails userDetails = userDetailsService
                                                .loadUserByUsername(
                                                                email);

                                if (!userDetails
                                                .isAccountNonLocked()) {
                                        denyRequest(
                                                        request,
                                                        response,
                                                        "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên");

                                        return;
                                }

                                if (!userDetails.isEnabled()) {
                                        denyRequest(
                                                        request,
                                                        response,
                                                        "Tài khoản không còn hoạt động");

                                        return;
                                }

                                if (jwtUtils.isTokenValid(
                                                token,
                                                userDetails)) {
                                        setAuthentication(
                                                        request,
                                                        userDetails);
                                } else {
                                        SecurityContextHolder
                                                        .clearContext();

                                        request.setAttribute(
                                                        JWT_ERROR_ATTRIBUTE,
                                                        "Token không hợp lệ");
                                }
                        }
                } catch (ExpiredJwtException exception) {
                        SecurityContextHolder
                                        .clearContext();

                        request.setAttribute(
                                        JWT_ERROR_ATTRIBUTE,
                                        "Token đã hết hạn");

                        log.debug(
                                        "JWT đã hết hạn");
                } catch (
                                JwtException
                                | IllegalArgumentException
                                | UsernameNotFoundException exception) {
                        SecurityContextHolder
                                        .clearContext();

                        request.setAttribute(
                                        JWT_ERROR_ATTRIBUTE,
                                        "Token không hợp lệ");

                        log.debug(
                                        "JWT không hợp lệ: {}",
                                        exception
                                                        .getClass()
                                                        .getSimpleName());
                }

                filterChain.doFilter(
                                request,
                                response);
        }

        private void setAuthentication(
                        HttpServletRequest request,
                        UserDetails userDetails) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                authentication.setDetails(
                                new WebAuthenticationDetailsSource()
                                                .buildDetails(request));

                SecurityContext securityContext = SecurityContextHolder
                                .createEmptyContext();

                securityContext.setAuthentication(
                                authentication);

                SecurityContextHolder.setContext(
                                securityContext);
        }

        private void denyRequest(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        String message) throws IOException {

                SecurityContextHolder
                                .clearContext();

                request.setAttribute(
                                ACCOUNT_FORBIDDEN_ATTRIBUTE,
                                message);

                jwtAccessDeniedHandler.handle(
                                request,
                                response,
                                new AccessDeniedException(
                                                message));
        }

        private String resolveToken(
                        HttpServletRequest request) {
                String authorizationHeader = request.getHeader(
                                HttpHeaders.AUTHORIZATION);

                if (!StringUtils.hasText(
                                authorizationHeader)
                                || !authorizationHeader
                                                .startsWith("Bearer ")) {
                        return null;
                }

                String token = authorizationHeader
                                .substring(7)
                                .trim();

                return StringUtils.hasText(token)
                                ? token
                                : null;
        }
}