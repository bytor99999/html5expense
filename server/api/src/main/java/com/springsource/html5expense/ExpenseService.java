package com.springsource.html5expense;

import java.util.Collection;
import java.util.List;

/**
 * User: Mark Spritzler
 * Date: 3/5/12
 * Time: 5:16 PM
 */
public interface ExpenseService {
    Collection<Expense> getExpensesForExpenseReport(Long reportId);

    /**
     * Adds the selected charges to the expense report.
     * Creates and returns a new expense for each charge.
     *
     * @param reportId  the expense report id
     * @param chargeIds the eligible charge ids
     * @return an expense for each charge
     */
    Collection<Expense> createExpenses(Long reportId, List<Long> chargeIds);

    Expense getExpense(Long expenseId);
}
