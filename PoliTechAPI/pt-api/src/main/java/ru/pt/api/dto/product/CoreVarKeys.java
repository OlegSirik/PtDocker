package ru.pt.api.dto.product;

public class CoreVarKeys {

    private CoreVarKeys() {}

    // Policy
    public static final String POLICY_NUMBER = "pl_policy_nr";
    public static final String POLICY_STATUS = "pl_status";
    public static final String POLICY_VERSION = "pl_version";

    // Product
    public static final String PRODUCT_CODE = "pl_product_code";
    public static final String PRODUCT_VERSION = "pl_product_version";

    public static String coverSumInsured(String cover) {
        return "co_" + cover + "_sumInsured";
    }

    public static String coverPremium(String cover) {
        return "co_" + cover + "_premium";
    }

    public static String coverDeductibleNr(String cover) {
        return "co_" + cover + "_deductibleNr";
    }
}
