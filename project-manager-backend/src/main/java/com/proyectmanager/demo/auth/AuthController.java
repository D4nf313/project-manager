package com.proyectmanager.demo.auth;

import com.proyectmanager.demo.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación.
 * Expone los dos endpoints del flujo de login de dos pasos.
 * Ambos son públicos — no requieren token para acceder.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login y selección de workspace")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     *
     * Primer paso — autenticación con credenciales.
     * Retorna los workspaces disponibles y un token temporal.
     *
     * Body: { "email": "...", "password": "..." }
     */
    @Operation(summary = "Login con credenciales",
            description = "Retorna token temporal y lista de workspaces")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/token
     *
     * Segundo paso — intercambio de contexto.
     * El frontend envía el workspaceId elegido y el token temporal
     * en el header Authorization: Bearer <tempToken>
     * Retorna el JWT final con rol embebido.
     *
     * Header: Authorization: Bearer <tempToken>
     * Body:   { "workspaceId": "uuid-del-workspace" }
     */

    @Operation(summary = "Intercambio de contexto",
            description = "Recibe workspaceId y retorna JWT final con rol embebido")
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> exchangeToken(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TokenRequest request
    ) {
        // Extraemos el token quitando el prefijo "Bearer "
        String tempToken = authHeader.substring(7);
        TokenResponse response = authService.exchangeToken(tempToken, request);
        return ResponseEntity.ok(response);
    }


}