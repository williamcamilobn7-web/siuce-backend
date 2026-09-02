package com.colegio.security;

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
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Público
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/", "/index.html", "/*.js", "/*.css").permitAll()

                        //Carga masiva
                        .requestMatchers("/api/carga-masiva/**")
                        .hasAuthority("ROLE_DISCIPLINA")

                        // Estudiantes
                        .requestMatchers(HttpMethod.GET, "/api/estudiantes/**")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")
                        .requestMatchers(HttpMethod.POST, "/api/estudiantes")
                        .hasAuthority("ROLE_DISCIPLINA")
                        .requestMatchers(HttpMethod.PUT, "/api/estudiantes/**")
                        .hasAuthority("ROLE_DISCIPLINA")
                        .requestMatchers(HttpMethod.DELETE, "/api/estudiantes/**")
                        .hasAuthority("ROLE_DISCIPLINA")

                        // Acudientes
                        .requestMatchers(HttpMethod.GET, "/api/acudientes/**")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")
                        .requestMatchers(HttpMethod.POST, "/api/acudientes/**")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/acudientes/**")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")

                        // Reportes — operaciones restringidas
                        .requestMatchers(HttpMethod.DELETE, "/api/reportes/**")
                        .hasAuthority("ROLE_DISCIPLINA")
                        .requestMatchers(HttpMethod.POST, "/api/reportes/*/firmar")
                        .hasAuthority("ROLE_DISCIPLINA")
                        .requestMatchers(HttpMethod.POST, "/api/reportes/*/enviar-siuce")
                        .hasAuthority("ROLE_DISCIPLINA")

                        //  Reportes — estadísticas y listado general
                        .requestMatchers(HttpMethod.GET, "/api/reportes/estadisticas")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")
                        .requestMatchers(HttpMethod.GET, "/api/reportes")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")

                        // Reportes — crear y editar
                        .requestMatchers(HttpMethod.POST, "/api/reportes")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/reportes/**")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")

                        //  Reportes — ver detalle e implicados (todos los roles)
                        .requestMatchers(HttpMethod.GET, "/api/reportes/**")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE",
                                "ROLE_ESTUDIANTE", "ROLE_ACUDIENTE")

                        //  Implicados — agregar
                        .requestMatchers(HttpMethod.POST, "/api/reportes/*/implicados")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/reportes/*/implicados/**")
                        .hasAuthority("ROLE_DISCIPLINA")

                        // ── Colegios y catálogo
                        .requestMatchers(HttpMethod.GET, "/api/colegios/**")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")
                        .requestMatchers(HttpMethod.GET, "/api/entidades-salud/**")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/entidades-salud/**")
                        .hasAuthority("ROLE_DISCIPLINA")
                        .requestMatchers(HttpMethod.POST, "/api/entidades-salud/**")
                        .hasAuthority("ROLE_DISCIPLINA")
                        .requestMatchers(HttpMethod.GET, "/api/policia/**")
                        .hasAnyAuthority("ROLE_DISCIPLINA", "ROLE_DOCENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/policia/**")
                        .hasAuthority("ROLE_DISCIPLINA")
                        .requestMatchers(HttpMethod.POST, "/api/policia/**")
                        .hasAuthority("ROLE_DISCIPLINA")

                        //  Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        config.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}