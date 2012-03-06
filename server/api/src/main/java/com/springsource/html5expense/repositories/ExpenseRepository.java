package com.springsource.html5expense.repositories;

import com.springsource.html5expense.Expense;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * User: Mark Spritzler
 * Date: 3/5/12
 * Time: 5:37 PM
 */
@Repository
public interface ExpenseRepository {
    
    public List<Expense> getExpensesForExpenseReport(Long reportId);
    
    public Expense getExpense(Long expenseId);
    
    public void save(Expense expense);
    
    public void delete(Expense expense);
}
