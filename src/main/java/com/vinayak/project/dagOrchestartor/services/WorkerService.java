package com.vinayak.project.dagOrchestartor.services;

import com.vinayak.project.dagOrchestartor.entities.ENUM.TaskStatus;
import com.vinayak.project.dagOrchestartor.entities.TaskExecution;
import com.vinayak.project.dagOrchestartor.kafka.TaskProducer;
import com.vinayak.project.dagOrchestartor.repositories.TaskExecutionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private static final Logger logger = LoggerFactory.getLogger(WorkerService.class);

    private final TaskExecutionRepo taskExecutionRepo;
    private final WorkFlowService workFlowService;
    private final TaskProducer taskProducer;


    public void executeTask(Long taskId) {

        int updatedRows = taskExecutionRepo.updateStatusIfMatches(taskId, TaskStatus.READY, TaskStatus.RUNNING);

        if (updatedRows == 0) {
            logger.warn("Failed to update task {} to RUNNING. It may have been picked by another worker.", taskId);
            return; // Another worker has already picked this task
        }

        try {
            simulateWork(); // Simulate task execution
            completeTaskExecution(taskId); // Mark task as completed and trigger dependent tasks
        } catch (Exception e) {
            failTaskExecution(taskId, e); // Handle task failure and retry logic
        }
    }

    private void simulateWork() throws InterruptedException {
        // Simulate work by sleeping for a random time between 1-3 seconds
        Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
    }

    @Transactional
    public void completeTaskExecution(Long taskId) {
        //Fetch the task
        TaskExecution task = taskExecutionRepo.findById(taskId).orElseThrow(() ->
                new RuntimeException("Task with ID " + taskId + " not found"));

        if ("B".equals(task.getTaskName())) {
            throw new RuntimeException("Forced failure");
        }

        if (task.getStatus() != TaskStatus.RUNNING) {
            logger.warn("Task {} is not in RUNNING state. Current state: {}. Skipping completion.", task.getTaskName(), task.getStatus());
            return; // Task is not in the expected state, skip processing
        }

        task.setStatus(TaskStatus.COMPLETED);
        taskExecutionRepo.save(task);

        logger.info("Task {} completed successfully", task.getTaskName());

        // Trigger dependent tasks
        logger.debug("Triggering dependent tasks for completed task: {}", task.getTaskName());
        workFlowService.triggerDependentTasks(task.getWorkFlowExecutionId(), task.getTaskName());
        workFlowService.checkAndUpdateWorkflowStatus(task.getWorkFlowExecutionId());

        logger.debug("Dependent task trigger completed for: {}", task.getTaskName());
        logger.debug("Task {} persisted to database with status: {}", task.getTaskName(), task.getStatus());


    }

    @Transactional
    public void failTaskExecution(Long taskId, Exception e) {
        TaskExecution task = taskExecutionRepo.findById(taskId).orElseThrow(() ->
                new RuntimeException("Task with ID " + taskId + " not found"));

        if (task.getStatus() != TaskStatus.RUNNING) {
            logger.warn("Task {} is not in RUNNING state. Current state: {}. Skipping failure handling.", task.getTaskName(), task.getStatus());
            return; // Task is not in the expected state, skip processing
        }

        handleTaskFailure(task, e);
    }

    private void handleTaskFailure(TaskExecution task, Exception e) {
        int currentRetry = task.getRetryCount();
        int maxRetries = task.getMaxRetries() != null ? task.getMaxRetries() : 3; // Default to 3 retries if not set

        logger.error("Task {} failed on attempt {}/{}. Error: {}", task.getTaskName(), currentRetry + 1, maxRetries, e.getMessage());

        if (currentRetry < maxRetries) {
            int retryCount = currentRetry + 1;
            task.setRetryCount(retryCount);
            task.setStatus(TaskStatus.READY); // Set back to READY for retry
            taskExecutionRepo.save(task);
            logger.info("Task {} will be retried. Current retry count: {}/{}", task.getTaskName(), retryCount, maxRetries);
            taskProducer.sendTask(task.getId(), task.getWorkFlowExecutionId().toString()); // Re-queue the task for retry

        } else {
            task.setStatus(TaskStatus.FAILED);
            logger.error("Task {} has reached max retry attempts and is marked as FAILED", task.getTaskName());
            taskExecutionRepo.save(task);
            workFlowService.checkAndUpdateWorkflowStatus(task.getWorkFlowExecutionId());
        }
    }
}

