package com.vinayak.project.dagOrchestartor.services;

import com.vinayak.project.dagOrchestartor.dto.TaskDefinition;
import com.vinayak.project.dagOrchestartor.dto.WorkFlowDefinitionJson;
import com.vinayak.project.dagOrchestartor.dto.WorkFlowRequest;
import com.vinayak.project.dagOrchestartor.entities.ENUM.TaskStatus;
import com.vinayak.project.dagOrchestartor.entities.TaskExecution;
import com.vinayak.project.dagOrchestartor.entities.WorkFlowDefinition;
import com.vinayak.project.dagOrchestartor.entities.WorkflowExecution;
import com.vinayak.project.dagOrchestartor.repositories.TaskExecutionRepo;
import com.vinayak.project.dagOrchestartor.repositories.WorkFlowDefinitionRepo;
import com.vinayak.project.dagOrchestartor.repositories.WorkFlowExecutionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkFlowService {
    private final WorkFlowDefinitionRepo workFlowDefinitionRepo;
    private final WorkFlowExecutionRepo workFlowExecutionRepo;
    private final TaskExecutionRepo taskExecutionRepo;
    private final ModelMapper modelMapper;

    public Long createWorkFlow(WorkFlowRequest workFlowRequest) {
        return Long.valueOf("12");
    }

    @Transactional
    public Long executeWorkFlow(Long id) {
        // Fetch Workflow Definition
        WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not Found"));

        // Create Workflow Execution
        WorkflowExecution workFlowExecution = WorkflowExecution.builder()
                .workflowDefinitionId(workFlowDefinition.getId())
                .status("RUNNING")
                .startedAt(LocalDateTime.now())
                .build();

        workFlowExecutionRepo.save(workFlowExecution);

        // Parse Workflow Definition JSON
        WorkFlowDefinitionJson workflowDefinitionJson;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            workflowDefinitionJson = objectMapper.readValue(workFlowDefinition.getDefinitionJson(), WorkFlowDefinitionJson.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Workflow JSON", e);
        }


        // Create Task Executions
        Map<String, TaskExecution> taskExecutionMap = new HashMap<>();
        List<TaskExecution> taskExecutions = new ArrayList<>();

        // Iterate through task definitions and create TaskExecution entities
        for (TaskDefinition taskDefinition : workflowDefinitionJson.getTasks()) {
            TaskExecution execution = TaskExecution.builder()
                    .workFlowExecutionId(workFlowExecution.getId())
                    .taskName(taskDefinition.getName())
                    .status(TaskStatus.CREATED)
                    .retryCount(0)
                    .build();

            taskExecutions.add(execution);
            taskExecutionMap.put(taskDefinition.getName(), execution);
        }
        taskExecutionRepo.saveAll(taskExecutions);


        // Update status of tasks with no dependencies to READY
        List<TaskExecution> readyTasks = new ArrayList<>();
        for (TaskDefinition taskDefinition : workflowDefinitionJson.getTasks()) {
            if (taskDefinition.getDependencies() == null || taskDefinition.getDependencies().isEmpty()) {
                TaskExecution execution = taskExecutionMap.get(taskDefinition.getName());
                execution.setStatus(TaskStatus.READY);

                readyTasks.add(execution);
            }
        }
        taskExecutionRepo.saveAll(readyTasks);

        return workFlowExecution.getId();

    }
}
