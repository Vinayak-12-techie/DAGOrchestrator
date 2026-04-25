package com.vinayak.project.dagOrchestartor.services;

import com.vinayak.project.dagOrchestartor.entities.ENUM.TaskStatus;
import com.vinayak.project.dagOrchestartor.entities.TaskExecution;
import com.vinayak.project.dagOrchestartor.repositories.TaskExecutionRepo;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private static final Logger logger = LoggerFactory.getLogger(WorkerService.class);

    private final TaskExecutionRepo taskExecutionRepo;
    private final WorkFlowService workFlowService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5); // Thread pool for concurrent task execution

    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    public void pollAndExecuteTasks() {
        logger.debug("===== Task Polling Cycle Started =====");

        List<TaskExecution> readyTasks = taskExecutionRepo.findByStatus(TaskStatus.READY);
        logger.info("Found {} tasks in READY status for execution", readyTasks.size());

        if (readyTasks.isEmpty()) {
            logger.debug("No tasks to execute. Waiting for next poll cycle...");
            return;
        }

        for (TaskExecution task : readyTasks) {
            logger.info("Executing task: {} (ID: {}, Workflow Execution: {})",
                    task.getTaskName(), task.getId(), task.getWorkFlowExecutionId());

            executorService.submit(() -> executeTask(task.getId()));
        }
        logger.debug("===== Task Polling Cycle Completed - Processed {} tasks =====", readyTasks.size());

    }

    @Transactional
    private void executeTask(Long taskId) {
        // Attempt to update task status to RUNNING atomically
        int updatedRows = taskExecutionRepo.updateStatusIfMatches(taskId, TaskStatus.READY, TaskStatus.RUNNING);

        if(updatedRows == 0) {
            logger.warn("Failed to update task {} to RUNNING. It may have been picked by another worker.", taskId);
            return; // Another worker has already picked this task
        }

        //Fetch the task
        TaskExecution task = taskExecutionRepo.findById(taskId).orElseThrow(() ->
                new RuntimeException("Task with ID " + taskId + " not found"));

        // Simulate Task Execution
        // Double-check status before execution, later we can implement a locking mechanism to prevent multiple workers from picking the same task
        try {
            logger.info("Task {} is now RUNNING", task.getTaskName());

            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000)); // Simulate time taken to execute task

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

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down WorkerService executor...");
        executorService.shutdown();
    }

}

