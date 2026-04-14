package com.vinayak.project.dagOrchestartor.repositories;

import com.vinayak.project.dagOrchestartor.entities.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkFlowExecutionRepo extends JpaRepository<WorkflowExecution,Long> {
}
