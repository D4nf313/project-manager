package com.proyectmanager.demo.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Lo que el frontend envía en el body del POST /api/projects
 * para crear un nuevo proyecto.
 * El workspaceId NO viene aquí — se extrae del JWT automáticamente.
 */
@Getter @Setter
public class ProjectRequest {

    @NotBlank(message = "El nombre es requerido")
    private String name;

    private String description;
}