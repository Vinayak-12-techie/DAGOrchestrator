package com.vinayak.project.dagOrchestartor.services;

import com.vinayak.project.dagOrchestartor.entities.ENUM.TaskStatus;
import com.vinayak.project.dagOrchestartor.entities.TaskExecution;
import com.vinayak.project.dagOrchestartor.repositories.TaskExecutionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private static final Logger logger = LoggerFactory.getLogger(WorkerService.class);
    
    private final TaskExecutionRepo taskExecutionRepo;
    private final WorkFlowService workFlowService;

    @Transactional
    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    public void pollAndExecuteTasks() {
        logger.debug("===== Task Polling Cycle Started =====");
        
        List<TaskExecution> readyTasks = taskExecutionRepo.findByStatus(TaskStatus.READY);
        logger.info("Found {} tasks in READY status for execution", readyTasks.size());
        
        if (readyTasks.isEmpty()) {
            logger.debug("No tasks to execute. Waiting for next poll cycle...");
            return;
        }
        
        for(TaskExecution task: readyTasks){
            logger.info("Executing task: {} (ID: {}, Workflow Execution: {})", 
                       task.getTaskName(), task.getId(), task.getWorkFlowExecutionId());
            
            // Change status to RUNNING
            task.setStatus(TaskStatus.RUNNING);
            taskExecutionRepo.save(task);
            logger.debug("Task {} status changed to RUNNING", task.getTaskName());

            // Simulate Task Execution
            try {
                logger.debug("Task {} execution in progress (simulating 2 second work)...", task.getTaskName());
                Thread.sleep(2000); // Simulate time taken to execute task
                
                task.setStatus(TaskStatus.COMPLETED);
                logger.info("Task {} completed successfully", task.getTaskName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                task.setStatus(TaskStatus.FAILED);
                logger.error("Task {} execution failed: {}", task.getTaskName(), e.getMessage(), e);
            }
            
            taskExecutionRepo.save(task);
            logger.debug("Task {} persisted to database with status: {}", task.getTaskName(), task.getStatus());

            // Trigger dependent tasks
            logger.debug("Triggering dependent tasks for completed task: {}", task.getTaskName());
            workFlowService.triggerDependentTasks(task.getWorkFlowExecutionId(), task.getTaskName());
            logger.debug("Dependent task trigger completed for: {}", task.getTaskName());
        }
        
        logger.debug("===== Task Polling Cycle Completed - Processed {} tasks =====", readyTasks.size());
    }
}
