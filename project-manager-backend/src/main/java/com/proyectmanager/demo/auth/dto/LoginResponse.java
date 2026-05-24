package com.proyectmanager.demo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

/**
 * Lo que el backend retorna tras un login exitoso.
 * Contiene el token temporal y la lista de workspaces
 * a los que tiene acceso el usuario — para que el frontend
 * los muestre en el selector de workspace.
 */
@Getter @Builder @AllArgsConstructor
public class LoginResponse {

    private String tempToken;       // token temporal — solo para el paso 2
    private String userId;
    private String name;
    private String email;
    private List<WorkspaceInfo> workspaces; // lista de workspaces disponibles

    /**
     * Clase interna que representa cada workspace
     * en la lista — incluye el rol del usuario en ese workspace.
     */
    @Getter @Builder @AllArgsConstructor
    public static class WorkspaceInfo {
        private String id;
        private String name;
        private String role;  // ADMIN, EDITOR o LECTOR
    }
}