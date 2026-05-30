package com.legalflow.repository;

import com.legalflow.entity.AgentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentLogRepository extends JpaRepository<AgentLog, Long> {
    List<AgentLog> findByTaskIdOrderByCreatedAtAsc(String taskId);
}
