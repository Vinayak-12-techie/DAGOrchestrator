package com.vinayak.project.dagOrchestartor.services;

import com.vinayak.project.dagOrchestartor.dto.TaskDefinition;
import com.vinayak.project.dagOrchestartor.dto.WorkFlowDefinitionJson;
import com.vinayak.project.dagOrchestartor.dto.WorkFlowRequest;
import com.vinayak.project.dagOrchestartor.entities.ENUM.TaskStatus;
import com.vinayak.project.dagOrchestartor.entities.TaskExecution;
import com.vinayak.project.dagOrchestartor.entities.WorkFlowDefinition;
import com.vinayak.project.dagOrchestartor.entities.WorkflowExecution;
import com.vinayak.project.dagOrchestartor.kafka.TaskProducer;
import com.vinayak.project.dagOrchestartor.repositories.TaskExecutionRepo;
import com.vinayak.project.dagOrchestartor.repositories.WorkFlowDefinitionRepo;
import com.vinayak.project.dagOrchestartor.repositories.WorkFlowExecutionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(WorkFlowService.class);
    
    private final WorkFlowDefinitionRepo workFlowDefinitionRepo;
    private final WorkFlowExecutionRepo workFlowExecutionRepo;
    private final TaskExecutionRepo taskExecutionRepo;
    private final ObjectMapper objectMapper;
    private final TaskProducer taskProducer;

    public Long createWorkFlow(WorkFlowRequest workFlowRequest) {
        logger.info("Creating workflow: name={}", workFlowRequest.getName());
        
        WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder()
                .name(workFlowRequest.getName())
                .definitionJson(workFlowRequest.getDefinitionJson())
                .version(1)
                .build();

        workFlowDefinitionRepo.save(workFlowDefinition);
        logger.info("Workflow created successfully with ID: {}", workFlowDefinition.getId());
        return workFlowDefinition.getId();
    }

    @Transactional
    public Long executeWorkFlow(Long id) {
        logger.info("Starting workflow execution for workflow ID: {}", id);
        
        // Fetch Workflow Definition: This is saved while creating workflow and it contains the JSON definition of workflow which is used to create task executions
        WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepo.findById(id)
                .orElseThrow(() -> {
                    logger.error("Workflow definition not found with ID: {}", id);
                    return new RuntimeException("Not Found");
                });
        
        logger.debug("Fetched workflow definition: {}", workFlowDefinition.getName());

        // Create Workflow Execution
        WorkflowExecution workFlowExecution = WorkflowExecution.builder()
                .workflowDefinitionId(workFlowDefinition.getId())
                .status("RUNNING")
                .startedAt(LocalDateTime.now())
                .build();

        workFlowExecution = workFlowExecutionRepo.save(workFlowExecution);
        logger.info("Workflow execution created with ID: {}, status: RUNNING", workFlowExecution.getId());

        // Parse Workflow Definition JSON
        WorkFlowDefinitionJson workflowDefinitionJson;
        try {
            workflowDefinitionJson = objectMapper.readValue(workFlowDefinition.getDefinitionJson(), WorkFlowDefinitionJson.class);
            logger.debug("Successfully parsed workflow definition JSON");
        } catch (Exception e) {
            logger.error("Failed to parse workflow definition JSON for workflow ID: {}", id, e);
            throw new RuntimeException("Invalid Workflow JSON", e);
        }


        // Create Task Executions
        Map<String, TaskExecution> taskExecutionMap = new HashMap<>();
        List<TaskExecution> taskExecutions = new ArrayList<>();

        // Iterate through task definitions and create TaskExecution entities
        logger.info("Creating task executions for {} tasks", workflowDefinitionJson.getTasks().size());
        for (TaskDefinition taskDefinition : workflowDefinitionJson.getTasks()) {
            TaskExecution execution = TaskExecution.builder()
                    .workFlowExecutionId(workFlowExecution.getId())
                    .taskName(taskDefinition.getName())
                    .status(TaskStatus.CREATED)
                    .retryCount(0)
                    .build();

            taskExecutions.add(execution);
            taskExecutionMap.put(taskDefinition.getName(), execution);
            logger.debug("Task created: name={}, status={}", taskDefinition.getName(), TaskStatus.CREATED);
        }
        taskExecutionRepo.saveAll(taskExecutions);
        logger.info("All {} tasks persisted to database", taskExecutions.size());


        // Update status of tasks with no dependencies to READY
        List<TaskExecution> readyTasks = new ArrayList<>();
        for (TaskDefinition taskDefinition : workflowDefinitionJson.getTasks()) {
            if (taskDefinition.getDependencies() == null || taskDefinition.getDependencies().isEmpty()) {
                TaskExecution execution = taskExecutionMap.get(taskDefinition.getName());
                execution.setStatus(TaskStatus.READY);

                readyTasks.add(execution);
                logger.info("Task marked as READY (no dependencies): {}", taskDefinition.getName());
            }
        }
        taskExecutionRepo.saveAll(readyTasks);
        for(TaskExecution readyTask : readyTasks) {
            taskProducer.sendTask(readyTask.getId(), workFlowExecution.getId().toString());
            logger.info("Task {} sent to Kafka for execution", readyTask.getTaskName());
        }

        logger.info("Updated {} tasks to READY status", readyTasks.size());

        logger.info("Workflow execution completed successfully. Execution ID: {}, Status: RUNNING with {} ready tasks", 
                   workFlowExecution.getId(), readyTasks.size());
        
        return workFlowExecution.getId();

    }

    @Transactional
    public void triggerDependentTasks(Long workflowExecutionId, String taskName) {
        logger.info("Triggering dependent tasks for workflow execution ID: {}, completed task: {}", workflowExecutionId, taskName);
        
        //2. Fetch workflow execution details to get workflow definition id
        WorkflowExecution workflowExecution = workFlowExecutionRepo.findById(workflowExecutionId)
                .orElseThrow(() -> {
                    logger.error("Workflow execution not found with ID: {}", workflowExecutionId);
                    return new RuntimeException("Workflow Execution Not Found");
                });
        
        logger.debug("Fetched workflow execution ID: {}, definition ID: {}", workflowExecutionId, workflowExecution.getWorkflowDefinitionId());

        //3. Load WorkFlow Definition -> Parse JSON
        WorkFlowDefinition workFlowDefinition = workFlowDefinitionRepo.findById(workflowExecution.getWorkflowDefinitionId())
                .orElseThrow(() -> {
                    logger.error("Workflow definition not found with ID: {}", workflowExecution.getWorkflowDefinitionId());
                    return new RuntimeException("Workflow Definition Not Found");
                });

        WorkFlowDefinitionJson workflowDefinitionJson;
        try {
            workflowDefinitionJson = objectMapper.readValue(workFlowDefinition.getDefinitionJson(), WorkFlowDefinitionJson.class);
            logger.debug("Successfully parsed workflow definition JSON");
        } catch (Exception e) {
            logger.error("Failed to parse workflow definition JSON for execution ID: {}", workflowExecutionId, e);
            throw new RuntimeException("Invalid Workflow JSON", e);
        }

        //4. Get All Task Executions for current workflow execution
        List<TaskExecution> taskExecutions = taskExecutionRepo.findByWorkFlowExecutionId(workflowExecutionId);
        logger.debug("Fetched {} task executions for workflow execution ID: {}", taskExecutions.size(), workflowExecutionId);
        
        Map<String, TaskExecution> taskExecutionMap = new HashMap<>();
        for (TaskExecution execution : taskExecutions) {
            taskExecutionMap.put(execution.getTaskName(), execution);
        }

        //5. Find dependent tasks and check if all dependencies are completed, if yes mark them as READY
        List<TaskExecution> readyTasks = new ArrayList<>();
        logger.debug("Checking {} task definitions for dependencies on task: {}", workflowDefinitionJson.getTasks().size(), taskName);
        
        for (TaskDefinition taskDefinition : workflowDefinitionJson.getTasks()) {
            if (taskDefinition.getDependencies() != null && taskDefinition.getDependencies().contains(taskName)) {
                logger.debug("Found task with dependency on {}: {}", taskName, taskDefinition.getName());
                
                boolean allDependenciesCompleted = true;
                for (String dependency : taskDefinition.getDependencies()) {
                    TaskExecution dependencyExecution = taskExecutionMap.get(dependency);
                    if (dependencyExecution == null || dependencyExecution.getStatus() != TaskStatus.COMPLETED) {
                        allDependenciesCompleted = false;
                        logger.debug("Dependency not completed: {} (status: {})", 
                                   dependency, 
                                   dependencyExecution != null ? dependencyExecution.getStatus() : "NOT_FOUND");
                        break;
                    }
                }

                if (allDependenciesCompleted) {
                    TaskExecution dependentTaskExecution =
                            taskExecutionMap.get(taskDefinition.getName());

                    if (dependentTaskExecution != null
                            && dependentTaskExecution.getStatus() == TaskStatus.CREATED) {

                        dependentTaskExecution.setStatus(TaskStatus.READY);
                        taskExecutionRepo.save(dependentTaskExecution);
//                        readyTasks.add(dependentTaskExecution);
                        taskProducer.sendTask(dependentTaskExecution.getId(), workflowExecutionId.toString());
                        logger.info("Task {} marked as READY - all dependencies completed", taskDefinition.getName());
                    }
                } else {
                    logger.debug("Task {} cannot be marked as READY - waiting for other dependencies", taskDefinition.getName());
                }
            }
        }

        logger.debug("Completed dependency check for workflow execution ID: {}", workflowExecutionId);
    }

    public void checkAndUpdateWorkflowStatus(Long workFlowExecutionId) {
        logger.info("Checking workflow status for execution ID: {}", workFlowExecutionId);
        
        List<TaskExecution> tasks = taskExecutionRepo.findByWorkFlowExecutionId(workFlowExecutionId);
        
        long completedCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long failedCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.FAILED).count();
        long runningCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.RUNNING).count();
        long readyCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.READY).count();
        long createdCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.CREATED).count();
        
        logger.debug("Workflow {} task status summary: Total={}, Completed={}, Failed={}, Running={}, Ready={}, Created={}", 
                   workFlowExecutionId, tasks.size(), completedCount, failedCount, runningCount, readyCount, createdCount);
        
        boolean allCompleted = tasks.stream().allMatch(t -> t.getStatus() == TaskStatus.COMPLETED);
        boolean anyFailed = tasks.stream().anyMatch(t -> t.getStatus() == TaskStatus.FAILED);

        WorkflowExecution workflowExecution = workFlowExecutionRepo.findById(workFlowExecutionId).orElseThrow(() -> {
            logger.error("Workflow Execution with ID {} not found", workFlowExecutionId);
            return new RuntimeException("Workflow Execution with ID " + workFlowExecutionId + " not found");
        });

        String previousStatus = workflowExecution.getStatus();
        if (allCompleted) {
            workflowExecution.setStatus("COMPLETED");
            workflowExecution.setEndedAt(java.time.LocalDateTime.now());
            logger.info("Workflow Execution {} status changed from {} to COMPLETED at {}", 
                       workFlowExecutionId, previousStatus, workflowExecution.getEndedAt());
        } else if (anyFailed) {
            workflowExecution.setStatus("FAILED");
            workflowExecution.setEndedAt(java.time.LocalDateTime.now());
            logger.info("Workflow Execution {} status changed from {} to FAILED due to {} failed task(s) at {}", 
                       workFlowExecutionId, previousStatus, failedCount, workflowExecution.getEndedAt());
        } else {
            logger.debug("Workflow Execution {} still in progress. Completed: {}/{}, Failed: {}", 
                        workFlowExecutionId, completedCount, tasks.size(), failedCount);
        }

        workFlowExecutionRepo.save(workflowExecution);
    }
}
