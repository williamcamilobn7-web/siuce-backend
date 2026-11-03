package com.colegio.security;

import com.colegio.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Se ejecuta cuando Google o GitHub confirmaron que la persona es quien dice ser.
 * Aquí se conecta OAuth con el resto del sistema:
 *  1. Saca el correo que verificó el proveedor.
 *  2. Pide a AuthService que lo busque entre los usuarios del sistema (no crea cuentas nuevas).
 *  3. Si existe, genera NUESTRO token JWT y manda el navegador de vuelta al frontend.
 * El token viaja en el fragmento de la URL (#token=...), que el navegador no envía a ningún servidor.
 */
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    // Puede no existir si no hay credenciales de OAuth configuradas, por eso se pide con ObjectProvider
    private final ObjectProvider<OAuth2AuthorizedClientService> clientes;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public OAuth2SuccessHandler(AuthService authService, ObjectProvider<OAuth2AuthorizedClientService> clientes) {
        this.authService = authService;
        this.clientes = clientes;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        String proveedor = oauth.getAuthorizedClientRegistrationId();   // "google" o "github"
        OAuth2User datos = oauth.getPrincipal();

        String email = null;
        boolean verificado = false;
        if ("google".equals(proveedor)) {
            email = datos.getAttribute("email");
            verificado = Boolean.TRUE.equals(datos.getAttribute("email_verified"));
        } else if ("github".equals(proveedor)) {
            // GitHub puede ocultar el correo en el perfil, por eso se consulta aparte
            email = correoVerificadoDeGithub(oauth);
            verificado = email != null;
        }

        // El baile de OAuth crea una sesión técnica; ya no se necesita, así el sistema sigue sin sesiones
        HttpSession sesion = req.getSession(false);
        if (sesion != null) sesion.invalidate();
        OAuth2AuthorizedClientService servicio = clientes.getIfAvailable();
        if (servicio != null) servicio.removeAuthorizedClient(proveedor, oauth.getName());

        try {
            String token = authService.loginConOAuth(proveedor, email, verificado, req.getRemoteAddr());
            res.sendRedirect(frontendUrl + "/oauth-callback#token=" + token);
        } catch (IllegalArgumentException e) {
            // e.getMessage() es un código corto: cuenta_no_vinculada o email_no_verificado
            res.sendRedirect(frontendUrl + "/oauth-callback#error=" + e.getMessage());
        }
    }

    // Consulta los correos de la cuenta de GitHub y devuelve el principal que esté verificado
    private String correoVerificadoDeGithub(OAuth2AuthenticationToken oauth) {
        OAuth2AuthorizedClientService servicio = clientes.getIfAvailable();
        if (servicio == null) return null;
        OAuth2AuthorizedClient cliente = servicio.loadAuthorizedClient(oauth.getAuthorizedClientRegistrationId(), oauth.getName());
        if (cliente == null) return null;
        try {
            List<Map<String, Object>> correos = RestClient.create().get()
                    .uri("https://api.github.com/user/emails")
                    .header("Authorization", "Bearer " + cliente.getAccessToken().getTokenValue())
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            if (correos == null) return null;
            return correos.stream()
                    .filter(c -> Boolean.TRUE.equals(c.get("verified")))
                    .sorted(Comparator.comparing((Map<String, Object> c) -> !Boolean.TRUE.equals(c.get("primary"))))
                    .map(c -> (String) c.get("email"))
                    .findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}