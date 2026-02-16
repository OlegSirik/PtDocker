package ru.pt.numbers.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public class NumberGeneratorRepositoryImpl implements NumberGeneratorRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Integer incrementAndGetCurrentValue(Long tid, Integer id) {
        Query query = entityManager.createNativeQuery(
                "UPDATE pt_number_generators SET current_value = current_value + 1 WHERE tid = :tid AND id = :id RETURNING current_value"
        );
        query.setParameter("tid", tid);
        query.setParameter("id", id);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).intValue() : null;
    }
}
