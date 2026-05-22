package ufpb.dcx.house.manager.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ufpb.dcx.house.manager.model.ExpensePayment;

public interface ExpensePaymentRepository extends JpaRepository<ExpensePayment, Long> {
    List<ExpensePayment> findAllByExpenseIdOrderByUserNameAsc(Long expenseId);

    Optional<ExpensePayment> findByExpenseIdAndUserId(Long expenseId, Long userId);
}
