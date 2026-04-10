package ru.pt.api.dto.numbers;

/**
 * DTO для создания алгоритма нумератора
 */
public class NumberGeneratorDescription {

    private Long id;
    //private String productCode;
    //private String code;
    private String mask;
    private ResetPolicy resetPolicy;
    private Long maxValue = 999_999L;
    private String xorMask;

    public NumberGeneratorDescription() {
    }

    public NumberGeneratorDescription(Long id, String mask, ResetPolicy resetPolicy, Long maxValue, String xorMask) {
        this.id = id;
      //  this.productCode = productCode;
       // this.code = productCode;
        this.mask = mask;
        this.resetPolicy = resetPolicy;
        this.maxValue = maxValue;
        this.xorMask = xorMask;
    }
/* 
    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }
*/
    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public ResetPolicy getResetPolicy() {
        return resetPolicy;
    }

    public void setResetPolicy(ResetPolicy resetPolicy) {
        this.resetPolicy = resetPolicy;
    }

    public Long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Long maxValue) {
        this.maxValue = maxValue;
    }

    public String getXorMask() {
        return xorMask;
    }

    public void setXorMask(String xorMask) {
        this.xorMask = xorMask;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /* 
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
*/
    @Override
    public String toString() {
        return "NumberGeneratorDescription{" +
                "id=" + id +
                //", productCode='" + productCode + '\'' +
                ", mask='" + mask + '\'' +
                ", resetPolicy=" + resetPolicy +
                ", maxValue=" + maxValue +
                ", xorMask='" + xorMask + '\'' +
                '}';
    }
}
