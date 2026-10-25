package cl.duoc.ms_orders.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalOrdersFilter extends OncePerRequestFilter {
    private final String expected;

    public InternalOrdersFilter(@Value("${ORDERS_INTERNAL_TOKEN}") String expected) {
        this.expected = expected;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/orders");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader("X-Internal-Token");
        String userId = request.getHeader("X-User-Id");
        boolean keyMatches = provided != null && expected != null && !expected.isBlank()
                && MessageDigest.isEqual(
                   expected.getBytes(StandardCharsets.UTF_8),
                   provided.getBytes(StandardCharsets.UTF_8));
        if (!keyMatches || userId == null || userId.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Requiere identidad autenticada del BFF");
            return;
        }
        chain.doFilter(request, response);
    }
}
