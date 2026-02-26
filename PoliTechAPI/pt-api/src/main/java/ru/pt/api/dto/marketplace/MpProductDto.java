package ru.pt.api.dto.marketplace;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product DTO for marketplace product list.
 */
public class MpProductDto {

    @JsonProperty("productId")
    private Long productId;

    @JsonProperty("productCode")
    private String productCode;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("packageCode")
    private String packageCode;

    @JsonProperty("packageName")
    private String packageName;

    @JsonProperty("term")
    private String term;

    @JsonProperty("covers")
    private List<MpCoverDto> covers;

    @JsonProperty("limit")
    private BigDecimal limit;

    @JsonProperty("premium")
    private BigDecimal premium;

    public MpProductDto() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPackageCode() {
        return packageCode;
    }

    public void setPackageCode(String packageCode) {
        this.packageCode = packageCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public List<MpCoverDto> getCovers() {
        return covers;
    }

    public void setCovers(List<MpCoverDto> covers) {
        this.covers = covers;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }
}
