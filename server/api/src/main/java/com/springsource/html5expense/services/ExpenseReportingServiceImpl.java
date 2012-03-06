/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.html5expense.services;

import com.springsource.html5expense.*;
import com.springsource.html5expense.repositories.ExpenseReportRepository;
import com.springsource.html5expense.repositories.ExpenseRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

/**
 * Implementation of the business logic for the expense reports and expense report charges.
 *
 * @author Josh Long
 * @author Roy Clarkson
 * @see ExpenseReportingService
 */
@Service
@Transactional
public class ExpenseReportingServiceImpl implements ExpenseReportingService {

    private Log log = LogFactory.getLog(getClass());

    @Inject
    private ExpenseReportRepository expenseReportRepository;

    @Inject
    private ExpenseRepository ExpenseRepository;

    @Inject
    private EligibleChargeService eligibleChargeServiceImpl;


    public void updateExpenseReportPurpose(Long reportId, String purpose) {
        ExpenseReport expenseReport = expenseReportRepository.findById(reportId);
        expenseReport.setPurpose(purpose);
    }

    public void deleteExpenseReport(Long expenseReportId) {
        Collection<Expense> expenses;
        expenses = ExpenseRepository.getExpensesForExpenseReport(expenseReportId);
        if (expenses.size() > 0) {
            List<Long> ids = new ArrayList<Long>();
            for (Expense e : expenses) {
                ids.add(e.getId());
            }
            eligibleChargeServiceImpl.restoreEligibleCharges(ids);
        }
        expenses = ExpenseRepository.getExpensesForExpenseReport(expenseReportId);
        log.debug("there are " + expenses.size() + " expenses  in the report #" + expenseReportId);
        ExpenseReport expenseReport = getExpenseReport(expenseReportId);
        expenseReportRepository.delete(expenseReport);
    }

    public Long createReport(String purpose) {
        ExpenseReport report = new ExpenseReport(purpose);
        expenseReportRepository.save(report);
        return report.getId();
    }

    public void submitReport(Long reportId) {
        ExpenseReport expenseReport = expenseReportRepository.findById(reportId);
        expenseReport.markInReview();
    }

    @Transactional(readOnly = true)
    public ExpenseReport getExpenseReport(Long reportId) {
        return expenseReportRepository.findById(reportId);
    }

    @Transactional(readOnly = true)
    public List<ExpenseReport> getOpenReports() {
        return expenseReportRepository.getOpenReports();
    }

    @Transactional(readOnly = true)
    public List<ExpenseReport> getSubmittedReports() {
        return expenseReportRepository.getSubmittedReports();
    }
}