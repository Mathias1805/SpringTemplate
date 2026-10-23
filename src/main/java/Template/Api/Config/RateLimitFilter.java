package Template.Api.Config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting para endpoints sensibles de autenticación.
 * Límite: 20 solicitudes por IP cada 60 segundos.
 *
 * trust-proxy: solo activar si la app está detrás de un reverse proxy (nginx, ALB, etc.)
 * que setea X-Forwarded-For de forma confiable. En despliegue directo dejar en false
 * para evitar que clientes falsifiquen su IP.
 *
 * En producción con alta carga considerar reemplazar el mapa en memoria por Redis (bucket4j-redis).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 20;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    private static final Set<String> RATE_LIMITED_PATHS = Set.of(
            "/Auth/Login",
            "/Auth/Register",
            "/Usuario/Login",
            "/Usuario/Register",
            "/Usuario/ResetPassword",
            "/Usuario/ConfirmReset"
    );

    // Solo leer X-Forwarded-For si se está detrás de un reverse proxy de confianza.
    @Value("${app.rate-limit.trust-proxy:false}")
    private boolean trustProxy;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isRateLimited(request.getRequestURI())) {
            String clientIp = resolveClientIp(request);
            Bucket bucket = buckets.computeIfAbsent(clientIp, ip -> createBucket());

            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        "{\"status\":429,\"error\":\"Too Many Requests\"," +
                        "\"message\":\"Demasiadas solicitudes. Intentá de nuevo en un minuto.\"," +
                        "\"timestamp\":" + System.currentTimeMillis() + "}"
                );
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String uri) {
        return RATE_LIMITED_PATHS.stream().anyMatch(uri::endsWith);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (trustProxy) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(MAX_REQUESTS)
                .refillIntervally(MAX_REQUESTS, REFILL_PERIOD)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
