package ufpb.dcx.house.manager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ufpb.dcx.house.manager.dto.ExpenseDtos.CreateExpenseRequest;
import ufpb.dcx.house.manager.dto.ExpenseDtos.ExpensePaymentResponse;
import ufpb.dcx.house.manager.dto.ExpenseDtos.ExpenseResponse;
import ufpb.dcx.house.manager.exception.ApiException;
import ufpb.dcx.house.manager.model.Expense;
import ufpb.dcx.house.manager.model.ExpensePayment;
import ufpb.dcx.house.manager.model.House;
import ufpb.dcx.house.manager.model.HouseMember;
import ufpb.dcx.house.manager.model.PaymentStatus;
import ufpb.dcx.house.manager.model.User;
import ufpb.dcx.house.manager.repository.ExpensePaymentRepository;
import ufpb.dcx.house.manager.repository.ExpenseRepository;
import ufpb.dcx.house.manager.repository.HouseMemberRepository;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpensePaymentRepository expensePaymentRepository;
    private final HouseMemberRepository houseMemberRepository;
    private final HouseService houseService;

    public ExpenseService(
        ExpenseRepository expenseRepository,
        ExpensePaymentRepository expensePaymentRepository,
        HouseMemberRepository houseMemberRepository,
        HouseService houseService
    ) {
        this.expenseRepository = expenseRepository;
        this.expensePaymentRepository = expensePaymentRepository;
        this.houseMemberRepository = houseMemberRepository;
        this.houseService = houseService;
    }

    @Transactional
    public ExpenseResponse createExpense(Long houseId, User currentUser, CreateExpenseRequest request) {
        House house = houseService.getAccessibleHouse(houseId, currentUser);

        Expense expense = new Expense();
        expense.setHouse(house);
        expense.setCreatedBy(currentUser);
        expense.setTitle(request.title().trim());
        expense.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        expense.setDueDate(request.dueDate());
        Expense savedExpense = expenseRepository.save(expense);

        List<HouseMember> members = houseMemberRepository.findAllByHouseIdOrderByJoinedAtAsc(houseId);
        BigDecimal share = savedExpense.getAmount()
            .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);

        for (HouseMember member : members) {
            ExpensePayment payment = new ExpensePayment();
            payment.setExpense(savedExpense);
            payment.setUser(member.getUser());
            payment.setAmount(share);
            expensePaymentRepository.save(payment);
        }

        return toExpenseResponse(savedExpense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listExpenses(Long houseId, User currentUser) {
        houseService.getAccessibleHouse(houseId, currentUser);
        return expenseRepository.findAllByHouseIdOrderByDueDateAsc(houseId)
            .stream()
            .map(this::toExpenseResponse)
            .toList();
    }

    @Transactional
    public ExpenseResponse markMyPaymentAsPaid(Long expenseId, User currentUser) {
        Expense expense = getAccessibleExpense(expenseId, currentUser);
        ExpensePayment payment = expensePaymentRepository.findByExpenseIdAndUserId(expenseId, currentUser.getId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment was not found."));

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());
        expensePaymentRepository.save(payment);

        return toExpenseResponse(expense);
    }

    @Transactional(readOnly = true)
    public Expense getAccessibleExpense(Long expenseId, User currentUser) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Expense was not found."));
        houseService.ensureHouseMember(expense.getHouse().getId(), currentUser.getId());
        return expense;
    }

    public ExpenseResponse toExpenseResponse(Expense expense) {
        List<ExpensePaymentResponse> payments = expensePaymentRepository
            .findAllByExpenseIdOrderByUserNameAsc(expense.getId())
            .stream()
            .map(this::toPaymentResponse)
            .toList();

        return new ExpenseResponse(
            expense.getId(),
            expense.getTitle(),
            expense.getAmount(),
            expense.getDueDate(),
            expense.getCreatedBy().getId(),
            payments
        );
    }

    private ExpensePaymentResponse toPaymentResponse(ExpensePayment payment) {
        User user = payment.getUser();
        return new ExpensePaymentResponse(
            payment.getId(),
            user.getId(),
            user.getName(),
            payment.getAmount(),
            payment.getStatus(),
            payment.getPaidAt()
        );
    }
}
