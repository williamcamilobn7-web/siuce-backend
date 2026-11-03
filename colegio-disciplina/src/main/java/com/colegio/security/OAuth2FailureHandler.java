package com.colegio.security;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Se ejecuta si el proveedor (Google o GitHub) falla o la persona cancela el inicio de sesión.
 * Devuelve al usuario a la pantalla del frontend con un código de error.
 */
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res,
                                        AuthenticationException ex) throws IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null) sesion.invalidate();
        res.sendRedirect(frontendUrl + "/oauth-callback#error=oauth_fallido");
    }
}