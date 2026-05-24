package com.proyectmanager.demo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Lo que el frontend envía en el body del POST /api/auth/token.
 * Solo el workspaceId — el userId viene en el token temporal
 * del header Authorization, no en el body.
 */
@Getter @Setter
public class TokenRequest {

    @NotBlank(message = "El workspaceId es requerido")
    private String workspaceId;
}