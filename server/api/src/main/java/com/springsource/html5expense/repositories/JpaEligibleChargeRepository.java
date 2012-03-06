package com.springsource.html5expense.repositories;

import com.springsource.html5expense.EligibleCharge;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;

/**
 * JPA Implementation of EligibleChargeRepository Data Access for
 * EligibleCharge domain object
 *
 * @author: Mark Spritzler
 */
@Repository
public class JpaEligibleChargeRepository implements EligibleChargeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Collection<EligibleCharge> getEligibleCharges() {
        return entityManager.createNamedQuery("eligibleCharge.getAll")
                                .getResultList();
    }

    @Override
    public List<EligibleCharge> getEligibleCharges(List<Long> chargeIds) {
        return entityManager.createNamedQuery("eligibleCharge.getAllByListOfIds")
                        .setParameter("ids", chargeIds)
                        .getResultList();
    }

    @Override
    public void removeAddedCharges(List<Long> chargeIds) {
        entityManager.createNamedQuery("eligibleCharge.deletedByListOfIdes")
                .setParameter("ids", chargeIds)
                .executeUpdate();
    }

    @Override
    public void merge(EligibleCharge eligibleCharge) {
        entityManager.merge(eligibleCharge);
    }

    @Override
    public void save(EligibleCharge eligibleCharge) {
        entityManager.persist(eligibleCharge);
    }

    @Override
    public void remove(EligibleCharge eligibleCharge) {
        entityManager.remove(eligibleCharge);
    }
}
