package com.vinayak.project.dagOrchestartor.repositories;

import com.vinayak.project.dagOrchestartor.entities.ENUM.TaskStatus;
import com.vinayak.project.dagOrchestartor.entities.TaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskExecutionRepo extends JpaRepository<TaskExecution,Long> {
    List<TaskExecution> findByWorkFlowExecutionId(Long workFlowExecutionId);

    List<TaskExecution> findByStatus(TaskStatus status);
}
