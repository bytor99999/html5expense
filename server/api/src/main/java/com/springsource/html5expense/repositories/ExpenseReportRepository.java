package com.springsource.html5expense.repositories;

import com.springsource.html5expense.ExpenseReport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User: Mark Spritzler
 * Date: 3/5/12
 * Time: 5:36 PM
 */
@Repository
public interface ExpenseReportRepository {

    public void save(ExpenseReport expenseReport);

    public void delete(ExpenseReport expenseReport);
    
    public ExpenseReport findById(Long expenseReportId);

    public List<ExpenseReport> getOpenReports();

    public List<ExpenseReport> getSubmittedReports();
}
