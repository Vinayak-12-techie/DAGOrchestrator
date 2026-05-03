package com.vinayak.project.dagOrchestartor.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "task-ready";

    public void sendTask(Long taskId, String key) {
        kafkaTemplate.send(TOPIC, key, String.valueOf(taskId));
    }
}
