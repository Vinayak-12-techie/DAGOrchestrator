package com.vinayak.project.dagOrchestartor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TaskDefinition {
    private String name;

    @JsonProperty("dependencies")
    private List<String> dependencies; // List of task names that this task depends on
}
