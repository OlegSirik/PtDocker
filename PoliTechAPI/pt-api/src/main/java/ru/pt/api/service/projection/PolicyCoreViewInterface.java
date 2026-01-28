package ru.pt.api.service.projection;

import java.math.BigDecimal;

import ru.pt.domain.model.VariableContext;

/**
 * Interface for policy core view operations
 */
public interface PolicyCoreViewInterface {


    /**
     * Gets the sum insured for a cover
     * @param cover Cover code
     * @return Sum insured value, or null if cover is null or not found
     */
    BigDecimal getCoverSumInsured(VariableContext ctx, String cover);

    /**
     * Gets the premium for a cover
     * @param cover Cover code
     * @return Premium value, or null if cover is null or not found
     */
    BigDecimal getCoverPremium(VariableContext ctx, String cover);

    /**
     * Gets the deductible number for a cover
     * @param cover Cover code
     * @return Deductible number, or null if cover is null or not found
     */
    Long getCoverDeductibleNr(VariableContext ctx, String cover);

    /**
     * Gets the minimum limit for a cover
     * @param cover Cover code
     * @return Minimum limit value, or null if cover is null or not found
     */
    BigDecimal getCoverLimitMin(VariableContext ctx, String cover);

    /**
     * Gets the maximum limit for a cover
     * @param cover Cover code
     * @return Maximum limit value, or null if cover is null or not found
     */
    BigDecimal getCoverLimitMax(VariableContext ctx, String cover);

    /**
     * Gets the package number
     * @return Package number, or null if not found
     */
    String getPackageNo(VariableContext ctx);
}
