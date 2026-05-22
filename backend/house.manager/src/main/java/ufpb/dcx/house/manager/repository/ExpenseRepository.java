package ufpb.dcx.house.manager.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ufpb.dcx.house.manager.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByHouseIdOrderByDueDateAsc(Long houseId);
}
