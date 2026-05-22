package ufpb.dcx.house.manager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import ufpb.dcx.house.manager.model.PaymentStatus;

public final class ExpenseDtos {

    private ExpenseDtos() {
    }

    public record CreateExpenseRequest(
        @NotBlank @Size(max = 120) String title,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull @FutureOrPresent LocalDate dueDate
    ) {
    }

    public record ExpensePaymentResponse(
        Long id,
        Long userId,
        String userName,
        BigDecimal amount,
        PaymentStatus status,
        Instant paidAt
    ) {
    }

    public record ExpenseResponse(
        Long id,
        String title,
        BigDecimal amount,
        LocalDate dueDate,
        Long createdById,
        List<ExpensePaymentResponse> payments
    ) {
    }
}
