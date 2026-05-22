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
import ufpb.dcx.house.manager.dto.ExpenseDtos.CreateExpenseRequest;
import ufpb.dcx.house.manager.dto.ExpenseDtos.ExpenseResponse;
import ufpb.dcx.house.manager.model.User;
import ufpb.dcx.house.manager.security.UserPrincipal;
import ufpb.dcx.house.manager.service.AuthService;
import ufpb.dcx.house.manager.service.ExpenseService;

@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final AuthService authService;
    private final ExpenseService expenseService;

    public ExpenseController(AuthService authService, ExpenseService expenseService) {
        this.authService = authService;
        this.expenseService = expenseService;
    }

    @GetMapping("/houses/{houseId}/expenses")
    public List<ExpenseResponse> listExpenses(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long houseId
    ) {
        return expenseService.listExpenses(houseId, currentUser(principal));
    }

    @PostMapping("/houses/{houseId}/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long houseId,
        @Valid @RequestBody CreateExpenseRequest request
    ) {
        return ResponseEntity.status(201).body(expenseService.createExpense(houseId, currentUser(principal), request));
    }

    @PostMapping("/expenses/{expenseId}/payments/me/paid")
    public ExpenseResponse markMyPaymentAsPaid(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long expenseId
    ) {
        return expenseService.markMyPaymentAsPaid(expenseId, currentUser(principal));
    }

    private User currentUser(UserPrincipal principal) {
        return authService.getCurrentUser(principal.id());
    }
}
