package com.proyectmanager.demo.project;

import com.proyectmanager.demo.project.dto.ProjectRequest;
import com.proyectmanager.demo.project.dto.ProjectResponse;
import com.proyectmanager.demo.workspace.Workspace;
import com.proyectmanager.demo.workspace.WorkspaceMembership;
import com.proyectmanager.demo.workspace.WorkspaceMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de proyectos.
 * Toda la lógica de negocio vive aquí, no en el controller.
 *
 * Patrón importante: en lugar de recibir el workspaceId como
 * parámetro del frontend, lo extraemos del SecurityContext —
 * que fue cargado por el JwtFilter con los datos del JWT.
 * Esto garantiza que el usuario no puede falsificar el workspace.
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceMembershipRepository membershipRepository;

    /**
     * Extrae el workspaceId del contexto de seguridad actual.
     * Recordar: en el JwtFilter guardamos el workspaceId
     * como "credentials" del objeto Authentication.
     */
    private UUID getWorkspaceIdFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // credentials contiene el workspaceId — lo pusimos en JwtFilter
        return UUID.fromString((String) auth.getCredentials());
    }

    /**
     * Extrae el userId del contexto de seguridad actual.
     * El userId fue guardado como "principal" en el JwtFilter.
     */
    private UUID getUserIdFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString((String) auth.getPrincipal());
    }

    /**
     * Extrae el rol del contexto de seguridad.
     * Spring Security guarda los roles como "ROLE_ADMIN", "ROLE_LECTOR", etc.
     * Quitamos el prefijo "ROLE_" para obtener solo "ADMIN", "LECTOR".
     */
    private String getRoleFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");
    }

    /**
     * GET /api/projects
     * Retorna todos los proyectos del workspace activo.
     * Cualquier rol autenticado puede ver proyectos.
     */
    public List<ProjectResponse> getProjects() {
        UUID workspaceId = getWorkspaceIdFromContext();

        return projectRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    /**
     * POST /api/projects
     * Crea un proyecto en el workspace activo.
     *
     * Validación de rol:
     * - ADMIN y EDITOR pueden crear proyectos
     * - LECTOR recibe 403 Forbidden
     *
     * El workspaceId viene del JWT, no del body del request —
     * el usuario no puede elegir en qué workspace crear el proyecto,
     * siempre es el workspace activo de su sesión.
     */
    public ProjectResponse createProject(ProjectRequest request) {
        String role = getRoleFromContext();

        // Validar que el rol permita crear proyectos
        if ("LECTOR".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para crear proyectos en este workspace"
            );
        }

        UUID workspaceId = getWorkspaceIdFromContext();
        UUID userId = getUserIdFromContext();

        // Verificar membresía activa — doble validación de seguridad
        WorkspaceMembership membership = membershipRepository
                .findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "No tienes acceso a este workspace"
                ));

        // Construir y guardar el proyecto
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .workspace(membership.getWorkspace())
                .build();

        Project saved = projectRepository.save(project);
        return ProjectResponse.from(saved);
    }
}