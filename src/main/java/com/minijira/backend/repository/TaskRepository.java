package com.minijira.backend.repository;

import com.minijira.backend.model.Task;
import com.minijira.backend.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssignedUserId(Long userId);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedUser LEFT JOIN FETCH t.project")
    List<Task> findAllWithDetails();
}
