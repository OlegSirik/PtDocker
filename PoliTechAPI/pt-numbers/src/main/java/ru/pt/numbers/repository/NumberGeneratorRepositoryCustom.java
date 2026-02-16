package ru.pt.numbers.repository;

/**
 * Custom repository methods for atomic number generation.
 */
public interface NumberGeneratorRepositoryCustom {

    /**
     * Atomically increment current_value and return the new value.
     * Uses UPDATE ... RETURNING to avoid race conditions.
     *
     * @param tid tenant id
     * @param id  generator id
     * @return new current_value, or null if no row was updated
     */
    Integer incrementAndGetCurrentValue(Long tid, Integer id);
}
