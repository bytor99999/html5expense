package com.springsource.html5expense.repositories;

import com.springsource.html5expense.ExpenseReport;
import com.springsource.html5expense.State;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * JPA implementation of ExpenseReportRepository. All Data operations regarding
 * ExpenseReport domain objects.
 *
 * @author: Mark Spritzler
 */
@Repository
public class JpaExpenseReportRepository implements ExpenseReportRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(ExpenseReport expenseReport) {
        entityManager.persist(expenseReport);
    }

    @Override
    public void delete(ExpenseReport expenseReport) {
        entityManager.remove(expenseReport);
    }

    @Override
    public ExpenseReport findById(Long expenseReportId) {
        return entityManager.find(ExpenseReport.class, expenseReportId);
    }

    @Override
    public List<ExpenseReport> getOpenReports() {
        return entityManager.createNamedQuery(
                        "expenseReport.findOpenReports")
                        .setParameter("new", State.NEW)
                        .setParameter("rejected", State.REJECTED)
                        .getResultList();
    }

    @Override
    public List<ExpenseReport> getSubmittedReports() {
        return entityManager.createNamedQuery(
                        "expenseReport.findSubmittedReports")
                        .setParameter("in_review", State.IN_REVIEW)
                        .setParameter("approved", State.APPROVED)
                        .getResultList();
    }
}
