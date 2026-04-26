package com.vinayak.project.dagOrchestartor.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "workflow_definitions")
public class WorkFlowDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private Integer version;
    
    @Column(columnDefinition = "TEXT")
    private String definitionJson; // Store the workflow definition as JSON string
}
