package com.proyectmanager.demo.workspace;

import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class WorkspaceMembershipId implements Serializable {
    private UUID user;
    private UUID workspace;
}