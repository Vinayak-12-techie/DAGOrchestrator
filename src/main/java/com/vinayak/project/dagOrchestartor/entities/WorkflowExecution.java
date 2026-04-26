package com.vinayak.project.dagOrchestartor.entities;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "workflow_executions")
public class WorkflowExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long workflowDefinitionId;
    
    private String status;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime endedAt;
}
