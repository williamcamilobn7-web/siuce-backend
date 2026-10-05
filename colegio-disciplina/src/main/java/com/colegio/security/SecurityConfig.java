package com.colegio.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

/**
 * Reglas de acceso: quién puede llamar a cada endpoint.
 * Las reglas se evalúan en orden y gana la primera que coincida, por eso las más específicas van arriba.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String DISC = "ROLE_DISCIPLINA";
    private static final String DOC  = "ROLE_DOCENTE";
    private static final String RECT = "ROLE_RECTOR";
    private static final String EST  = "ROLE_ESTUDIANTE";
    private static final String ACU  = "ROLE_ACUDIENTE";

    private final JwtFilter jwtFilter;

    @Value("${app.cors.origins:http://localhost:4200}")
    private String corsOrigins;

    public SecurityConfig(JwtFilter jwtFilter) { this.jwtFilter = jwtFilter; }

    // BCrypt: hash de una sola vía con sal, resistente a fuerza bruta (requisito RS-002)
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> c.configurationSource(corsSource()))
            // CSRF desactivado porque el token va en el header Authorization y no en una cookie
            .csrf(csrf -> csrf.disable())
            // Sin sesión en el servidor: cada petición se autentica con su token
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Sin token o con token inválido se responde 401 (por defecto Spring respondería 403)
            .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> res.sendError(401, "No autenticado")))
            .authorizeHttpRequests(auth -> auth
                // Orden de las reglas: lo público primero y al final "cualquier otra cosa requiere login"
                // Público
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/login-estudiante").permitAll()
                .requestMatchers("/", "/index.html", "/*.js", "/*.css").permitAll()
                // Solo personal de disciplina crea usuarios
                .requestMatchers("/api/auth/registro").hasAuthority(DISC)
                .requestMatchers("/api/auth/usuarios/*/cerrar-sesiones").hasAuthority(DISC)

                .requestMatchers("/api/carga-masiva/**").hasAuthority(DISC)
                .requestMatchers(HttpMethod.POST, "/api/acudientes/carga-masiva").hasAuthority(DISC)

                // Estudiantes
                .requestMatchers(HttpMethod.GET, "/api/estudiantes/**").hasAnyAuthority(DISC, DOC, RECT)
                .requestMatchers("/api/estudiantes/**").hasAuthority(DISC)

                // Acudientes
                .requestMatchers(HttpMethod.GET, "/api/acudientes/**").hasAnyAuthority(DISC, DOC, RECT)
                .requestMatchers("/api/acudientes/**").hasAnyAuthority(DISC, DOC)

                // Reportes: operaciones restringidas
                .requestMatchers(HttpMethod.DELETE, "/api/reportes/**").hasAuthority(DISC)
                .requestMatchers(HttpMethod.POST, "/api/reportes/*/firmar").hasAuthority(RECT)
                .requestMatchers(HttpMethod.POST, "/api/reportes/*/enviar-siuce").hasAnyAuthority(DISC, RECT)
                .requestMatchers(HttpMethod.PUT, "/api/reportes/**").hasAuthority(DISC)
                .requestMatchers(HttpMethod.POST, "/api/reportes").hasAnyAuthority(DISC, DOC)
                .requestMatchers(HttpMethod.POST, "/api/reportes/*/implicados").hasAnyAuthority(DISC, DOC)

                // Ojo: en el detalle de un reporte entran también estudiantes y acudientes, pero el controller
                // verifica que solo vean los reportes donde están implicados
                // Reportes: lectura
                .requestMatchers(HttpMethod.GET, "/api/reportes/estadisticas").hasAnyAuthority(DISC, DOC, RECT)
                .requestMatchers(HttpMethod.GET, "/api/reportes").hasAnyAuthority(DISC, DOC, RECT)
                .requestMatchers(HttpMethod.GET, "/api/reportes/*/implicados/**").hasAnyAuthority(DISC, DOC, RECT)
                // detalle: el controller valida que estudiantes/acudientes solo vean lo suyo
                .requestMatchers(HttpMethod.GET, "/api/reportes/*").hasAnyAuthority(DISC, DOC, RECT, EST, ACU)

                // Catálogos
                .requestMatchers(HttpMethod.GET, "/api/colegios/**", "/api/entidades-salud/**", "/api/policia/**")
                    .hasAnyAuthority(DISC, DOC, RECT)
                .requestMatchers("/api/colegios/**", "/api/entidades-salud/**", "/api/policia/**")
                    .hasAuthority(DISC)

                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Solo el frontend configurado puede llamar a la API desde un navegador (antes estaba abierto a cualquiera)
    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(corsOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
