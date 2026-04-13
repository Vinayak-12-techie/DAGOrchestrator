package com.vinayak.project.dagOrchestartor.services;

import com.vinayak.project.dagOrchestartor.dto.WorkFlowRequest;
import com.vinayak.project.dagOrchestartor.entities.WorkFlowDefinition;
import com.vinayak.project.dagOrchestartor.entities.WorkflowExecution;
import com.vinayak.project.dagOrchestartor.repositories.WorkFlowDefinitionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WorkFlowService {
    private final WorkFlowDefinitionRepo workFlowDefinitionRepo;

    public Long createWorkFlow(WorkFlowRequest workFlowRequest){
        return Long.valueOf("12");
    }

    public Long executeWorkFlow(Long id) {
        WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepo.findById(id)
                .orElseThrow(()-> new RuntimeException("Not Found"));

      WorkflowExecution workFlowExecution = WorkflowExecution.builder()
                .workflowDefinitionId(workFlowDefinition.getId())
                .status("RUNNING")
                .startedAt(LocalDateTime.now())
                .build();

      return id;

    }
}
