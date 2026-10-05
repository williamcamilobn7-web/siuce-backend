package com.colegio.security;

import com.colegio.repository.TokenRevocadoRepository;
import com.colegio.repository.UsuarioRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Se ejecuta en cada petición. Un JWT firmado y vigente NO es suficiente por sí solo,
 * así que además de validar la firma revisamos tres cosas en la base de datos:
 *  1. que el token no haya sido revocado (cierre de sesión),
 *  2. que el usuario todavía exista,
 *  3. que el token no sea anterior a "tokensValidosDesde" (cambio de contraseña o cierre masivo).
 * El rol se toma de la base y no del token, así un cambio de rol se aplica de inmediato.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepo;
    private final TokenRevocadoRepository revocadoRepo;

    public JwtFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepo, TokenRevocadoRepository revocadoRepo) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepo = usuarioRepo;
        this.revocadoRepo = revocadoRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token) && !revocadoRepo.existsById(jwtUtil.extractJti(token))) {
                usuarioRepo.findByUsername(jwtUtil.extractUsername(token)).ifPresent(u -> {
                    // El claim "iat" se guarda en segundos, por eso se compara contra la fecha truncada a segundos
                    boolean emitidoDespues = u.getTokensValidosDesde() == null ||
                            !jwtUtil.extractIssuedAt(token).isBefore(u.getTokensValidosDesde().truncatedTo(ChronoUnit.SECONDS));
                    if (emitidoDespues) {
                        var auth = new UsernamePasswordAuthenticationToken(
                                u.getUsername(), null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                });
            }
        }
        // Si no se autenticó nada, Spring responde 401 más adelante (ver SecurityConfig)
        chain.doFilter(request, response);
    }
}