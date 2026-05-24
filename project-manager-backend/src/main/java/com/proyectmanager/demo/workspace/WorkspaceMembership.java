package com.proyectmanager.demo.workspace;

import com.proyectmanager.demo.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "workspace_memberships")
@IdClass(WorkspaceMembershipId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkspaceMembership {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public enum Role {
        ADMIN, EDITOR, LECTOR
    }
}