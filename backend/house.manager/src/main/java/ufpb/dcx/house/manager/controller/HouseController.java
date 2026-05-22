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
import ufpb.dcx.house.manager.dto.DashboardResponse;
import ufpb.dcx.house.manager.dto.HouseDtos.AddMemberRequest;
import ufpb.dcx.house.manager.dto.HouseDtos.CreateHouseRequest;
import ufpb.dcx.house.manager.dto.HouseDtos.HouseResponse;
import ufpb.dcx.house.manager.dto.HouseDtos.MemberResponse;
import ufpb.dcx.house.manager.model.User;
import ufpb.dcx.house.manager.security.UserPrincipal;
import ufpb.dcx.house.manager.service.AuthService;
import ufpb.dcx.house.manager.service.DashboardService;
import ufpb.dcx.house.manager.service.HouseService;

@RestController
@RequestMapping("/api/houses")
public class HouseController {

    private final AuthService authService;
    private final HouseService houseService;
    private final DashboardService dashboardService;

    public HouseController(AuthService authService, HouseService houseService, DashboardService dashboardService) {
        this.authService = authService;
        this.houseService = houseService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public List<HouseResponse> listHouses(@AuthenticationPrincipal UserPrincipal principal) {
        return houseService.listHouses(currentUser(principal));
    }

    @PostMapping
    public ResponseEntity<HouseResponse> createHouse(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody CreateHouseRequest request
    ) {
        return ResponseEntity.status(201).body(houseService.createHouse(currentUser(principal), request));
    }

    @GetMapping("/{houseId}/members")
    public List<MemberResponse> listMembers(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long houseId
    ) {
        return houseService.listMembers(houseId, currentUser(principal));
    }

    @PostMapping("/{houseId}/members")
    public ResponseEntity<MemberResponse> addMember(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long houseId,
        @Valid @RequestBody AddMemberRequest request
    ) {
        return ResponseEntity.status(201).body(houseService.addMember(houseId, currentUser(principal), request));
    }

    @GetMapping("/{houseId}/dashboard")
    public DashboardResponse getDashboard(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long houseId
    ) {
        return dashboardService.getDashboard(houseId, currentUser(principal));
    }

    private User currentUser(UserPrincipal principal) {
        return authService.getCurrentUser(principal.id());
    }
}
