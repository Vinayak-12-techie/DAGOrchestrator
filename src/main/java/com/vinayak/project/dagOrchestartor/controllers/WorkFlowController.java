package com.vinayak.project.dagOrchestartor.controllers;

import com.vinayak.project.dagOrchestartor.dto.WorkFlowRequest;
import com.vinayak.project.dagOrchestartor.services.WorkFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkFlowController {
    private final WorkFlowService workFlowService;

    @PostMapping
    public ResponseEntity<Long> createWorkFlow(@RequestBody WorkFlowRequest workFlowRequest){
        return ResponseEntity.ok(workFlowService.createWorkFlow(workFlowRequest));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Long> executeWorkFlow(@PathVariable Long id){
        return ResponseEntity.ok(workFlowService.executeWorkFlow(id));
    }
}
