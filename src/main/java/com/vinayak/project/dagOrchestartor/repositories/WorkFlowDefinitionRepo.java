package com.vinayak.project.dagOrchestartor.repositories;

import com.vinayak.project.dagOrchestartor.entities.WorkFlowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkFlowDefinitionRepo extends JpaRepository<WorkFlowDefinition, Long> {
}
