package com.taskflow.repository;

import com.taskflow.model.Task;
import com.taskflow.model.Task.TaskPriority;
import com.taskflow.model.Task.TaskStatus;
import com.taskflow.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser(User user, Sort sort);

    List<Task> findByUserAndStatus(User user, TaskStatus status, Sort sort);

    List<Task> findByUserAndPriority(User user, TaskPriority priority, Sort sort);

    List<Task> findByUserAndStatusAndPriority(User user, TaskStatus status, TaskPriority priority, Sort sort);

    @Query("SELECT t FROM Task t WHERE t.user = :user " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Task> findByUserFiltered(
            @Param("user") User user,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("search") String search,
            Sort sort);
}
