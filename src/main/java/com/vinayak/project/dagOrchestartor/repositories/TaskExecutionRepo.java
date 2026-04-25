package com.vinayak.project.dagOrchestartor.repositories;

import com.vinayak.project.dagOrchestartor.entities.ENUM.TaskStatus;
import com.vinayak.project.dagOrchestartor.entities.TaskExecution;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskExecutionRepo extends JpaRepository<TaskExecution,Long> {
    List<TaskExecution> findByWorkFlowExecutionId(Long workFlowExecutionId);

    List<TaskExecution> findByStatus(TaskStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE TaskExecution t SET t.status = :newStatus WHERE t.id = :id AND t.status = :currentStaus")
    int updateStatusIfMatches(@Param("id") Long id,
                              @Param("currentStaus") TaskStatus currentStatus,
                              @Param("newStatus") TaskStatus newStatus);
}
