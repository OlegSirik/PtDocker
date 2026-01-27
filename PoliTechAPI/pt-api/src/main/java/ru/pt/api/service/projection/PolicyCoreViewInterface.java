package ru.pt.api.service.projection;

import java.math.BigDecimal;

/**
 * Interface for policy core view operations
 */
public interface PolicyCoreViewInterface {


    /**
     * Gets the sum insured for a cover
     * @param cover Cover code
     * @return Sum insured value, or null if cover is null or not found
     */
    BigDecimal getCoverSumInsured(String cover);

    /**
     * Gets the premium for a cover
     * @param cover Cover code
     * @return Premium value, or null if cover is null or not found
     */
    BigDecimal getCoverPremium(String cover);

    /**
     * Gets the deductible number for a cover
     * @param cover Cover code
     * @return Deductible number, or null if cover is null or not found
     */
    Long getCoverDeductibleNr(String cover);

    /**
     * Gets the minimum limit for a cover
     * @param cover Cover code
     * @return Minimum limit value, or null if cover is null or not found
     */
    BigDecimal getCoverLimitMin(String cover);

    /**
     * Gets the maximum limit for a cover
     * @param cover Cover code
     * @return Maximum limit value, or null if cover is null or not found
     */
    BigDecimal getCoverLimitMax(String cover);

    /**
     * Gets the package number
     * @return Package number, or null if not found
     */
    String getPackageNo();
}
