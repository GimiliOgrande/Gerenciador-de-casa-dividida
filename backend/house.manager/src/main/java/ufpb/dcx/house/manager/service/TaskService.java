package ufpb.dcx.house.manager.service;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ufpb.dcx.house.manager.dto.TaskDtos.CreateTaskRequest;
import ufpb.dcx.house.manager.dto.TaskDtos.TaskResponse;
import ufpb.dcx.house.manager.exception.ApiException;
import ufpb.dcx.house.manager.model.House;
import ufpb.dcx.house.manager.model.HouseTask;
import ufpb.dcx.house.manager.model.TaskStatus;
import ufpb.dcx.house.manager.model.User;
import ufpb.dcx.house.manager.repository.HouseTaskRepository;
import ufpb.dcx.house.manager.repository.UserRepository;

@Service
public class TaskService {

    private final HouseTaskRepository houseTaskRepository;
    private final UserRepository userRepository;
    private final HouseService houseService;

    public TaskService(
        HouseTaskRepository houseTaskRepository,
        UserRepository userRepository,
        HouseService houseService
    ) {
        this.houseTaskRepository = houseTaskRepository;
        this.userRepository = userRepository;
        this.houseService = houseService;
    }

    @Transactional
    public TaskResponse createTask(Long houseId, User currentUser, CreateTaskRequest request) {
        House house = houseService.getAccessibleHouse(houseId, currentUser);

        User assignedTo = null;
        if (request.assignedToUserId() != null) {
            houseService.ensureHouseMember(houseId, request.assignedToUserId());
            assignedTo = userRepository.findById(request.assignedToUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assigned user was not found."));
        }

        HouseTask task = new HouseTask();
        task.setHouse(house);
        task.setCreatedBy(currentUser);
        task.setAssignedTo(assignedTo);
        task.setTitle(request.title().trim());
        task.setDescription(trimToNull(request.description()));
        task.setDueDate(request.dueDate());

        return toTaskResponse(houseTaskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(Long houseId, User currentUser) {
        houseService.getAccessibleHouse(houseId, currentUser);
        return houseTaskRepository.findAllByHouseIdOrderByDueDateAsc(houseId)
            .stream()
            .map(this::toTaskResponse)
            .toList();
    }

    @Transactional
    public TaskResponse markTaskAsDone(Long taskId, User currentUser) {
        HouseTask task = houseTaskRepository.findById(taskId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task was not found."));
        houseService.ensureHouseMember(task.getHouse().getId(), currentUser.getId());

        task.setStatus(TaskStatus.DONE);
        task.setCompletedAt(Instant.now());
        return toTaskResponse(houseTaskRepository.save(task));
    }

    public TaskResponse toTaskResponse(HouseTask task) {
        User assignedTo = task.getAssignedTo();
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getDueDate(),
            task.getStatus(),
            assignedTo == null ? null : assignedTo.getId(),
            assignedTo == null ? null : assignedTo.getName(),
            task.getCompletedAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
