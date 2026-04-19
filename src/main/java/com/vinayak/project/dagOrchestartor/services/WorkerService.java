package com.vinayak.project.dagOrchestartor.services;

import com.vinayak.project.dagOrchestartor.entities.ENUM.TaskStatus;
import com.vinayak.project.dagOrchestartor.entities.TaskExecution;
import com.vinayak.project.dagOrchestartor.repositories.TaskExecutionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private final TaskExecutionRepo taskExecutionRepo;
    private final WorkFlowService workFlowService;

    @Transactional
    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    public void pollAndExecuteTasks() {
        List<TaskExecution> readyTasks = taskExecutionRepo.findByStatus(TaskStatus.READY);
        for(TaskExecution task: readyTasks){
            task.setStatus(TaskStatus.RUNNING);
            taskExecutionRepo.save(task);

            // Simulate Task Execution
            try {
                Thread.sleep(2000); // Simulate time taken to execute task
                task.setStatus(TaskStatus.COMPLETED);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                task.setStatus(TaskStatus.FAILED);
            }
            taskExecutionRepo.save(task);

            workFlowService.triggerDependentTasks(task.getWorkFlowExecutionId(), task.getTaskName());
        }
    }
}
