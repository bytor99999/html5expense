package com.springsource.html5expense.services;

import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReport;
import com.springsource.html5expense.repositories.EligibleChargeRepository;
import com.springsource.html5expense.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class ExpenseServiceImpl {

    @Inject
    private ExpenseReportingServiceImpl expenseReportingServiceImpl;

    @Inject
    private EligibleChargeRepository eligibleChargeRepository;
    
    @Inject
    private ExpenseRepository expenseRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Collection<Expense> getExpensesForExpenseReport(Long reportId) {
        List<Expense> expenseCollection =
                expenseRepository.getExpensesForExpenseReport(reportId);

        // consistent sorting
        Collections.sort(expenseCollection, new ExpenseComparator());

        return expenseCollection;
    }

    @Transactional(readOnly = true)
    public Expense getExpense(Long expenseId) {
        return expenseRepository.getExpense(expenseId);
    }

    public Collection<Expense> createExpenses(Long reportId, List<Long> chargeIds) {
        ExpenseReport report = expenseReportingServiceImpl.getExpenseReport(reportId);
        List<Expense> expenses = new ArrayList<Expense>();
        List<EligibleCharge> charges = eligibleChargeRepository.getEligibleCharges(chargeIds);
        for (EligibleCharge charge : charges) {
            Expense expense = report.createExpense(charge);
            expenseRepository.save(expense);
            expenses.add(expense);
        }
        eligibleChargeRepository.removeAddedCharges(chargeIds);
        return expenses;
    }

    private static class ExpenseComparator implements Comparator<Expense> {
        @Override
        public int compare(Expense expense, Expense expense1) {
            return expense.getId().compareTo(expense1.getId());
        }

    }
}