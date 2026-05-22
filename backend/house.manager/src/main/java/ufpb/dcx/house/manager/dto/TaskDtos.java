package ufpb.dcx.house.manager.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import ufpb.dcx.house.manager.model.TaskStatus;

public final class TaskDtos {

    private TaskDtos() {
    }

    public record CreateTaskRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 600) String description,
        @NotNull @FutureOrPresent LocalDate dueDate,
        Long assignedToUserId
    ) {
    }

    public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDate dueDate,
        TaskStatus status,
        Long assignedToUserId,
        String assignedToName,
        Instant completedAt
    ) {
    }
}
