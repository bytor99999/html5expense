package com.springsource.html5expense.repositories;

import com.springsource.html5expense.Expense;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * JPA implementation of Repository for Expense domain object
 * 
 * @author: Mark Spritzler
 */
@Repository
public class JpaExpenseRepository implements ExpenseRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Expense> getExpensesForExpenseReport(Long reportId) {
        return entityManager.createNamedQuery("expense.getAllExpensesForExpenseReport")
                .getResultList();
    }

    @Override
    public Expense getExpense(Long expenseId) {
        return entityManager.find(Expense.class, expenseId);
    }

    @Override
    public void save(Expense expense) {
        entityManager.persist(expense);
    }

    @Override
    public void delete(Expense expense) {
        entityManager.remove(expense);
    }
}
