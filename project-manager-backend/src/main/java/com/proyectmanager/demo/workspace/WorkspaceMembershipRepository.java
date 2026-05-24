package com.proyectmanager.demo.workspace;

import com.proyectmanager.demo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMembershipRepository
        extends JpaRepository<WorkspaceMembership, WorkspaceMembershipId> {

    List<WorkspaceMembership> findByUser(User user);

    Optional<WorkspaceMembership> findByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);
}