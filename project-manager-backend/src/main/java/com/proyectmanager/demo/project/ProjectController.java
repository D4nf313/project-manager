package com.proyectmanager.demo.project;

import com.proyectmanager.demo.project.dto.ProjectRequest;
import com.proyectmanager.demo.project.dto.ProjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de proyectos.
 * Expone los endpoints GET y POST /api/projects.
 * La validación de autenticación la hace Spring Security automáticamente
 * antes de que el request llegue aquí — gracias al JwtFilter y SecurityConfig.
 * La validación de rol la hace el ProjectService.
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Proyectos", description = "Gestión de proyectos por workspace")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;


    @Operation(summary = "Listar proyectos",
            description = "Retorna proyectos del workspace activo. Cualquier rol puede acceder.")
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects() {
        return ResponseEntity.ok(projectService.getProjects());
    }


    @Operation(summary = "Crear proyecto",
            description = "Crea un proyecto en el workspace activo. Solo ADMIN o EDITOR.")
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request
    ) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}