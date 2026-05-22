package ufpb.dcx.house.manager.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ufpb.dcx.house.manager.dto.TaskDtos.CreateTaskRequest;
import ufpb.dcx.house.manager.dto.TaskDtos.TaskResponse;
import ufpb.dcx.house.manager.model.User;
import ufpb.dcx.house.manager.security.UserPrincipal;
import ufpb.dcx.house.manager.service.AuthService;
import ufpb.dcx.house.manager.service.TaskService;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final AuthService authService;
    private final TaskService taskService;

    public TaskController(AuthService authService, TaskService taskService) {
        this.authService = authService;
        this.taskService = taskService;
    }

    @GetMapping("/houses/{houseId}/tasks")
    public List<TaskResponse> listTasks(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long houseId
    ) {
        return taskService.listTasks(houseId, currentUser(principal));
    }

    @PostMapping("/houses/{houseId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long houseId,
        @Valid @RequestBody CreateTaskRequest request
    ) {
        return ResponseEntity.status(201).body(taskService.createTask(houseId, currentUser(principal), request));
    }

    @PostMapping("/tasks/{taskId}/done")
    public TaskResponse markTaskAsDone(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long taskId
    ) {
        return taskService.markTaskAsDone(taskId, currentUser(principal));
    }

    private User currentUser(UserPrincipal principal) {
        return authService.getCurrentUser(principal.id());
    }
}
