package com.springsource.html5expense.repositories;

import com.springsource.html5expense.EligibleCharge;

import java.util.Collection;
import java.util.List;

/**
 * User: Mark Spritzler
 * Date: 3/5/12
 * Time: 5:37 PM
 */
public interface EligibleChargeRepository {

    public Collection<EligibleCharge> getEligibleCharges();

    public List<EligibleCharge> getEligibleCharges(List<Long> chargeIds);

    public void removeAddedCharges(List<Long> chargeIds);

    public void merge(EligibleCharge eligibleCharge);

    public void save(EligibleCharge eligibleCharge);

    public void remove(EligibleCharge eligibleCharge);

}
