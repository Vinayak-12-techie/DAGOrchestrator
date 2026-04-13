package com.vinayak.project.dagOrchestartor.dto;

import lombok.Data;

@Data
public class WorkFlowRequest {
    private String name;
    private Integer version;
    private Integer definitionJson;
}
