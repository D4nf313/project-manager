package com.proyectmanager.demo.project.dto;

import com.proyectmanager.demo.project.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Lo que el backend retorna al listar o crear proyectos.
 * Nunca exponemos la entidad JPA directamente — siempre un DTO
 * para controlar exactamente qué datos salen al frontend.
 */
@Getter @Builder @AllArgsConstructor
public class ProjectResponse {

    private String id;
    private String name;
    private String description;
    private String workspaceId;
    private String workspaceName;
    private LocalDateTime createdAt;

    /**
     * Método estático de conversión — transforma una entidad Project
     * en un ProjectResponse listo para serializar a JSON.
     */
    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .id(project.getId().toString())
                .name(project.getName())
                .description(project.getDescription())
                .workspaceId(project.getWorkspace().getId().toString())
                .workspaceName(project.getWorkspace().getName())
                .createdAt(project.getCreatedAt())
                .build();
    }
}