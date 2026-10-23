package Template.Api.Config;

import Template.Api.JWT.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.applyPermitDefaultValues();
                    config.addAllowedHeader("*");
                    // DEVELOPMENT: CORS abierto a todos los orígenes.
                    // En producción reemplazar con dominios explícitos:
                    //   config.addAllowedOrigin("https://tu-frontend.com");
                    config.addAllowedOriginPattern("*");
                    config.addAllowedMethod(HttpMethod.GET);
                    config.addAllowedMethod(HttpMethod.POST);
                    config.addAllowedMethod(HttpMethod.PUT);
                    config.addAllowedMethod(HttpMethod.DELETE);
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authRequest -> authRequest
                        // Públicos — sin autenticación
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/Auth/Login").permitAll()
                        .requestMatchers("/Usuario/Login").permitAll()
                        .requestMatchers("/Usuario/Register").permitAll()
                        .requestMatchers("/Usuario/Validate").permitAll()
                        .requestMatchers("/Usuario/ResetPassword").permitAll()
                        .requestMatchers("/Usuario/ConfirmReset").permitAll()
                        // Swagger UI / OpenAPI
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                        // Requiere rol ADMIN
                        .requestMatchers("/Auth/Register").hasAuthority("ADMIN")
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )
                .sessionManagement(sessionManager ->
                        sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
