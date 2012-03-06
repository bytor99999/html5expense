package com.springsource.html5expense.services;

import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.EligibleChargeService;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReportingService;
import com.springsource.html5expense.ExpenseService;
import com.springsource.html5expense.config.ComponentConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the {@link ExpenseReportingServiceImpl JPA expense reporting service}.
 *
 * @author Josh Long
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {ComponentConfig.class})
@TransactionConfiguration(defaultRollback = true)
@Transactional
@ActiveProfiles("local")
public class TestJpaExpenseReportingService {

    @Inject
    private ExpenseReportingService expenseReportingService;

    @Inject
    private ExpenseService expenseService;

    @Inject
    private EligibleChargeService eligibleChargeService;


    private String purpose = "SFO face to face";

    @Test
    public void testCreatingAnExpenseReport() throws Throwable {
        Long reportId = expenseReportingService.createReport(this.purpose);
        assertNotNull(reportId);
        assertTrue(reportId > 0);
    }

    @Test
    public void testCreatingAnExpenseReportExpenses() throws Throwable {
        Long reportId = expenseReportingService.createReport(this.purpose);
        Collection<EligibleCharge> chargeCollection = eligibleChargeService.getEligibleCharges();
        List<Long> ids = new ArrayList<Long>(chargeCollection.size());
        for (EligibleCharge eligibleCharge : chargeCollection)
            ids.add(eligibleCharge.getId());
        Collection<Expense> expenseCollection = expenseService.createExpenses(reportId, ids);
        assertEquals(expenseCollection.size(), ids.size());
        assertEquals(expenseCollection.size(), expenseService.getExpensesForExpenseReport(reportId).size());
    }
}
