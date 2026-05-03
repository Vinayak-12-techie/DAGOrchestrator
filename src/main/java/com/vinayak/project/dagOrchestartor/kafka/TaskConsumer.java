package com.vinayak.project.dagOrchestartor.kafka;

import com.vinayak.project.dagOrchestartor.services.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskConsumer {
    private final WorkerService workerService;

    @KafkaListener(topics = "task-ready", groupId = "worker-group")
    public void consumeTask(String taskIdString) {
        Long taskId = Long.parseLong(taskIdString);
        workerService.executeTask(taskId);
    }
}
