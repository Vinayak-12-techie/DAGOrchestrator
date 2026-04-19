package com.vinayak.project.dagOrchestartor.repositories;

import com.vinayak.project.dagOrchestartor.entities.TaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.ScopedValue;
import java.util.List;

public interface TaskExecutionRepo extends JpaRepository<TaskExecution,Long> {
    List<TaskExecution> findByWorkFlowExecutionId(Long workFlowExecutionId);

    TaskExecution findByWorkFlowExecutionIdAndTaskName(Long workflowExecutionId, String taskName);
}
