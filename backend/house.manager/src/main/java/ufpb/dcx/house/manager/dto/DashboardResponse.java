package ufpb.dcx.house.manager.dto;

import java.util.List;
import ufpb.dcx.house.manager.dto.ExpenseDtos.ExpenseResponse;
import ufpb.dcx.house.manager.dto.HouseDtos.HouseResponse;
import ufpb.dcx.house.manager.dto.HouseDtos.MemberResponse;
import ufpb.dcx.house.manager.dto.TaskDtos.TaskResponse;

public record DashboardResponse(
    HouseResponse house,
    List<MemberResponse> members,
    List<ExpenseResponse> expenses,
    List<TaskResponse> tasks
) {
}
