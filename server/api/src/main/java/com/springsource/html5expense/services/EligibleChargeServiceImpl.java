package com.springsource.html5expense.services;


import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.EligibleChargeService;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReport;
import com.springsource.html5expense.repositories.EligibleChargeRepository;
import com.springsource.html5expense.repositories.ExpenseReportRepository;
import com.springsource.html5expense.repositories.ExpenseRepository;
import com.springsource.html5expense.services.utilities.MongoDbGridFsUtilities;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
//import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class EligibleChargeServiceImpl implements EligibleChargeService {

    @Inject
    private ExpenseReportRepository ExpenseReportRepository;

    @Inject
    private EligibleChargeRepository eligibleChargeRepository;

    @Inject
    private ExpenseRepository expenseRepository;

    @Inject
    private MongoDbGridFsUtilities mongoDbGridFsUtilities;

    private String mongoDbGridFsFileBucket = "expenseReports";

    //private File tmpDir = new File(SystemUtils.getUserHome(), "receipts");

    public InputStream retrieveReceipt(Long expenseId) {
        Expense e = expenseRepository.getExpense(expenseId);
        String fn = fileNameForReceipt(e);
        return mongoDbGridFsUtilities.read(mongoDbGridFsFileBucket, fn);
    }

    @Transactional(readOnly = true)
    public Collection<EligibleCharge> getEligibleCharges() {
        return eligibleChargeRepository.getEligibleCharges();
    }

    public EligibleCharge createEligibleCharge(Date date, String merchant, String category, BigDecimal amt) {
        EligibleCharge charge = new EligibleCharge(date, merchant, category, amt);
        eligibleChargeRepository.save(charge);
        return charge;
    }

    public void restoreEligibleCharges(List<Long> expenseIds) {
        for (Long l : expenseIds) {
            Expense e = expenseRepository.getExpense(l);
            EligibleCharge eligibleCharge = createEligibleCharge(e.getDate(), e.getMerchant(), e.getCategory(), e.getAmount());
            if (eligibleCharge != null) {
                expenseRepository.delete(e);
            }
        }
    }

    public String attachReceipt(Long reportId, Long expenseId, String ext, byte[] receiptBytes) {
        String reportAndExpenseKey = keyForExpenseReceipt(reportId, expenseId);
        Expense expense = expenseRepository.getExpense(expenseId);
        ExpenseReport report = ExpenseReportRepository.findById(reportId);
        report.attachReceipt(expenseId, reportAndExpenseKey, ext);
        writeExpenseReceiptToDurableMedia(expense, receiptBytes);
        return reportAndExpenseKey;
    }

    public List<EligibleCharge> getEligibleCharges(List<Long> chargeIds) {
        return eligibleChargeRepository.getEligibleCharges(chargeIds);
    }

    private String keyForExpenseReceipt(Long reportId, Long expenseId) {
        return "receipt-" + reportId + "-" + (expenseId) + "";
    }

    private String fileNameForReceipt(String key, String ext) {
        return key + "." + ext;
    }

    public void removeAddedCharges(List<Long> chargeIds) {
        eligibleChargeRepository.removeAddedCharges(chargeIds);
    }

    /**
     * Delegates to MongoDB gridfs to persist the receipts themselves.
     *
     * @param expense      the expense to which the receipt was to be attached
     * @param receiptBytes the bytes for the receipt image, itself.
     */
    private void writeExpenseReceiptToDurableMedia(Expense expense, byte[] receiptBytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(receiptBytes);
        String fileNameOfReceipt = fileNameForReceipt(expense);
        mongoDbGridFsUtilities.write(mongoDbGridFsFileBucket, byteArrayInputStream, fileNameOfReceipt, null);
    }

    private String fileNameForReceipt(Expense e) {
        return fileNameForReceipt(e.getReceipt(), e.getReceiptExtension());
    }
}