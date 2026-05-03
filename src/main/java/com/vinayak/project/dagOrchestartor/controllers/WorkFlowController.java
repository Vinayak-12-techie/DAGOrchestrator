package com.vinayak.project.dagOrchestartor.controllers;

import com.vinayak.project.dagOrchestartor.dto.WorkFlowRequest;
import com.vinayak.project.dagOrchestartor.services.WorkFlowService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkFlowController {
    private static final Logger logger = LoggerFactory.getLogger(WorkFlowController.class);
    private final WorkFlowService workFlowService;

    @PostMapping
    public ResponseEntity<Long> createWorkFlow(@RequestBody WorkFlowRequest workFlowRequest){
        logger.info("API Request - createWorkFlow: name={}", workFlowRequest.getName());
        try {
            Long workflowId = workFlowService.createWorkFlow(workFlowRequest);
            logger.info("API Response - createWorkFlow: workflowId={}", workflowId);
            return ResponseEntity.ok(workflowId);
        } catch (Exception e) {
            logger.error("API Error - createWorkFlow failed for workflow: {}", workFlowRequest.getName(), e);
            throw e;
        }
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Long> executeWorkFlow(@PathVariable Long id){
        logger.info("API Request - executeWorkFlow: workflowId={}", id);
        try {
            Long executionId = workFlowService.executeWorkFlow(id);
            logger.info("API Response - executeWorkFlow: executionId={}", executionId);
            return ResponseEntity.ok(executionId);
        } catch (Exception e) {
            logger.error("API Error - executeWorkFlow failed for workflow ID: {}", id, e);
            throw e;
        }
    }
}
