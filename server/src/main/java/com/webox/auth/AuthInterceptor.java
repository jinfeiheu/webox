package com.webox.auth;

import com.webox.common.api.BizException;
import com.webox.common.api.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Authenticates every /api/** request via Bearer JWT and enforces {@link RequireRole}.
 * /api/auth/login and /api/auth/register are excluded in WebConfig.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true; // CORS preflight
        }
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Browser EventSource cannot attach custom headers — accept token as query param too.
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            String paramToken = request.getParameter("token");
            if (paramToken != null) {
                header = BEARER_PREFIX + paramToken;
            }
        }
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        AuthUser user;
        try {
            user = jwtService.parse(header.substring(BEARER_PREFIX.length()));
        } catch (Exception e) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Your session has expired. Please log in again.");
        }
        AuthContext.set(user);

        if (handler instanceof HandlerMethod handlerMethod) {
            RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
            if (requireRole == null) {
                requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            }
            if (requireRole != null && user.role() != requireRole.value()) {
                throw new BizException(ErrorCode.FORBIDDEN);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        AuthContext.clear();
    }
}
