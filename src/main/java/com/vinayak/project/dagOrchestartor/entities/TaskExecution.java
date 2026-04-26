package com.vinayak.project.dagOrchestartor.entities;

import com.vinayak.project.dagOrchestartor.entities.ENUM.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "task_executions")
public class TaskExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long workFlowExecutionId;

    private String taskName;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private Integer retryCount;

    private String workerId;

    private Integer maxRetries;

    private LocalDateTime nextRetryTime;
}
