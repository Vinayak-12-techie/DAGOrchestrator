package com.vinayak.project.dagOrchestartor.services;

import com.vinayak.project.dagOrchestartor.config.MapperConfig;
import com.vinayak.project.dagOrchestartor.dto.TaskDefinition;
import com.vinayak.project.dagOrchestartor.dto.WorkFlowDefinitionJson;
import com.vinayak.project.dagOrchestartor.dto.WorkFlowRequest;
import com.vinayak.project.dagOrchestartor.entities.TaskExecution;
import com.vinayak.project.dagOrchestartor.entities.WorkFlowDefinition;
import com.vinayak.project.dagOrchestartor.entities.WorkflowExecution;
import com.vinayak.project.dagOrchestartor.repositories.TaskExecutionRepo;
import com.vinayak.project.dagOrchestartor.repositories.WorkFlowDefinitionRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkFlowService {
    private final WorkFlowDefinitionRepo workFlowDefinitionRepo;
    private final TaskExecutionRepo taskExecutionRepo;
    private final ModelMapper modelMapper;

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

      WorkFlowDefinitionJson workflowDefinitionJson = modelMapper.map(workFlowDefinition.getDefinitionJson(), WorkFlowDefinitionJson.class);

      Map<String, TaskExecution> taskExecutionMap = new HashMap<>();

      for(TaskDefinition taskDefinition : workflowDefinitionJson.getTasks()){
         TaskExecution execution = TaskExecution.builder()
                 .workFlowExecutionId(workFlowExecution.getId())
                 .taskName(taskDefinition.getName())
                 .status("CREATED")
                 .retryCount(0)
                 .build();

            taskExecutionRepo.save(execution);

            taskExecutionMap.put(taskDefinition.getName(), execution);
      }

      for(TaskDefinition taskDefinition : workflowDefinitionJson.getTasks()){
          if(taskDefinition.getDependencies().isEmpty()){
              TaskExecution execution = taskExecutionMap.get(taskDefinition.getName());
              execution.setStatus("READY");

              taskExecutionRepo.save(execution);
          }
      }

      return id;

    }
}
