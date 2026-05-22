package ufpb.dcx.house.manager.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ufpb.dcx.house.manager.dto.DashboardResponse;
import ufpb.dcx.house.manager.model.House;
import ufpb.dcx.house.manager.model.User;

@Service
public class DashboardService {

    private final HouseService houseService;
    private final ExpenseService expenseService;
    private final TaskService taskService;

    public DashboardService(HouseService houseService, ExpenseService expenseService, TaskService taskService) {
        this.houseService = houseService;
        this.expenseService = expenseService;
        this.taskService = taskService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long houseId, User currentUser) {
        House house = houseService.getAccessibleHouse(houseId, currentUser);
        return new DashboardResponse(
            houseService.toHouseResponse(house),
            houseService.listMembers(houseId, currentUser),
            expenseService.listExpenses(houseId, currentUser),
            taskService.listTasks(houseId, currentUser)
        );
    }
}
