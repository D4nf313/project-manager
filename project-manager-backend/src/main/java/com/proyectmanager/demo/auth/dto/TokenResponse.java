package com.proyectmanager.demo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Lo que retorna el POST /api/auth/token.
 * El token final con workspaceId y rol embebidos —
 * este es el que el frontend usa en todos los requests siguientes.
 */
@Getter @Builder @AllArgsConstructor
public class TokenResponse {
    private String token;       // JWT final con userId + workspaceId + role
    private String workspaceId;
    private String workspaceName;
    private String role;        // para que el frontend sepa qué mostrar/ocultar
}