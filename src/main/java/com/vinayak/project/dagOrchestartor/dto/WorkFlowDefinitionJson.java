package com.vinayak.project.dagOrchestartor.dto;

import lombok.Data;

import java.util.List;

@Data
public class WorkFlowDefinitionJson {
    private List<TaskDefinition> tasks;
}
