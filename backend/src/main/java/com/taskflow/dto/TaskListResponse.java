package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TaskListResponse {

    private List<TaskResponse> tasks;
    private int count;

    public TaskListResponse() {
    }

    public TaskListResponse(List<TaskResponse> tasks) {
        this.tasks = tasks;
        this.count = tasks.size();
    }

    @JsonProperty("tasks")
    public List<TaskResponse> getTasks() { return tasks; }
    public void setTasks(List<TaskResponse> tasks) { this.tasks = tasks; this.count = tasks == null ? 0 : tasks.size(); }

    @JsonProperty("count")
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
