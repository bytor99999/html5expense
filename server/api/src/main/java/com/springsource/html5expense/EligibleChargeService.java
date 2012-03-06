package com.springsource.html5expense;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * User: Mark Spritzler
 * Date: 3/5/12
 * Time: 5:17 PM
 */
public interface EligibleChargeService {
    InputStream retrieveReceipt(Long expenseId);

    /**
     * Responsible for installing new {@link com.springsource.html5expense.EligibleCharge}s into the database
     */
    EligibleCharge createEligibleCharge(Date date, String merchant, String category, BigDecimal amt);

    void restoreEligibleCharges(List<Long> expenseIds);

    /**
     * Retrieves the charges that are eligible to be expensed.
     * The user is expected to add one or more of these charges to the report.
     *
     * @return the list of eligible charges
     */
    Collection<EligibleCharge> getEligibleCharges();

    /**
     * Attach a receipt to an expense.
     *
     * @param reportId     the expense report id
     * @param receiptBytes the receipt data as a byte array
     * @param ext          the extension of the uploaded media
     * @return a pointer to the receipt
     */
    String attachReceipt(Long reportId, Long expenseId, String ext, byte[] receiptBytes);

    public void removeAddedCharges(List<Long> chargeIds);
}
