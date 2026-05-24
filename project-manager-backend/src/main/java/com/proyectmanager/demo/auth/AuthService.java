package com.proyectmanager.demo.auth;

import com.proyectmanager.demo.auth.dto.*;
import com.proyectmanager.demo.security.JwtUtil;
import com.proyectmanager.demo.user.User;
import com.proyectmanager.demo.user.UserRepository;
import com.proyectmanager.demo.workspace.WorkspaceMembership;
import com.proyectmanager.demo.workspace.WorkspaceMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de autenticación — contiene toda la lógica del proceso
 * de dos pasos: login y selección de workspace.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WorkspaceMembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * PASO 1 — Login con credenciales.
     *
     * Flujo:
     * 1. Buscamos el usuario por email
     * 2. Verificamos la contraseña con BCrypt
     * 3. Cargamos todos los workspaces a los que pertenece
     * 4. Generamos un token TEMPORAL (5 min) sin workspace ni rol
     * 5. Retornamos usuario + workspaces + token temporal
     */
    public LoginResponse login(LoginRequest request) {

        // 1. Buscar usuario — si no existe retornamos 401
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciales inválidas"
                ));

        // 2. Verificar contraseña con BCrypt
        // passwordEncoder.matches() compara el texto plano con el hash en BD
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Credenciales inválidas"
            );
        }

        // 3. Cargar membresías — todos los workspaces del usuario con su rol
        List<WorkspaceMembership> memberships = membershipRepository.findByUser(user);

        // 4. Convertir membresías a WorkspaceInfo para la respuesta
        List<LoginResponse.WorkspaceInfo> workspaceInfos = memberships.stream()
                .map(m -> LoginResponse.WorkspaceInfo.builder()
                        .id(m.getWorkspace().getId().toString())
                        .name(m.getWorkspace().getName())
                        .role(m.getRole().name())
                        .build())
                .toList();

        // 5. Generar token temporal — solo identifica al usuario
        String tempToken = jwtUtil.generateTempToken(user.getId());

        return LoginResponse.builder()
                .tempToken(tempToken)
                .userId(user.getId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .workspaces(workspaceInfos)
                .build();
    }

    /**
     * PASO 2 — Intercambio de contexto (selección de workspace).
     *
     * Flujo:
     * 1. Validamos el token temporal del header
     * 2. Extraemos el userId del token temporal
     * 3. Verificamos que el usuario pertenezca al workspace solicitado
     * 4. Obtenemos el rol del usuario en ese workspace
     * 5. Generamos el JWT FINAL con userId + workspaceId + rol
     *
     * @param tempToken   el token temporal del header Authorization
     * @param request     contiene el workspaceId seleccionado
     */
    public TokenResponse exchangeToken(String tempToken, TokenRequest request) {

        // 1. Validar que el token sea temporal y no haya expirado
        if (!jwtUtil.isTokenValid(tempToken, "TEMP")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Token temporal inválido o expirado"
            );
        }

        // 2. Extraer el userId del token temporal
        UUID userId = UUID.fromString(jwtUtil.extractUserId(tempToken));
        UUID workspaceId = UUID.fromString(request.getWorkspaceId());

        // 3. Verificar que el usuario pertenezca al workspace solicitado
        // Si no pertenece retornamos 403 — no tiene acceso
        WorkspaceMembership membership = membershipRepository
                .findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "No tienes acceso a este workspace"
                ));

        // 4. Obtener el rol en este workspace específico
        String role = membership.getRole().name();

        // 5. Generar JWT final con contexto completo
        String token = jwtUtil.generateToken(userId, workspaceId, role);

        return TokenResponse.builder()
                .token(token)
                .workspaceId(workspaceId.toString())
                .workspaceName(membership.getWorkspace().getName())
                .role(role)
                .build();
    }
}