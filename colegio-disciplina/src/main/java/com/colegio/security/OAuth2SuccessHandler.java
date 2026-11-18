package com.colegio.security;

import com.colegio.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Se ejecuta cuando Google confirma quién es la persona.
 * Busca el correo entre los usuarios del sistema (no crea cuentas nuevas), genera nuestro
 * token JWT, lo deja en la cookie HttpOnly y devuelve el navegador al frontend.
 */
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtCookieService cookies;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public OAuth2SuccessHandler(AuthService authService, JwtCookieService cookies) {
        this.authService = authService;
        this.cookies = cookies;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication authentication) throws IOException {
        OAuth2User datos = (OAuth2User) authentication.getPrincipal();
        String email = datos.getAttribute("email");
        boolean verificado = Boolean.TRUE.equals(datos.getAttribute("email_verified"));

        // La sesión técnica del baile de OAuth ya no se necesita: se destruye
        HttpSession sesion = req.getSession(false);
        if (sesion != null) sesion.invalidate();

        try {
            String token = authService.loginConOAuth("google", email, verificado, req.getRemoteAddr());
            // El token va en la cookie; el frontend ya no lo ve ni lo guarda
            res.addHeader(HttpHeaders.SET_COOKIE, cookies.crear(token).toString());
            res.sendRedirect(frontendUrl + "/oauth-callback");
        } catch (IllegalArgumentException e) {
            // Código corto: cuenta_no_vinculada o email_no_verificado
            res.sendRedirect(frontendUrl + "/oauth-callback#error=" + e.getMessage());
        }
    }
}