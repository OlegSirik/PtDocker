package ru.pt.api.dto.addon;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolicyAddOnDto {

    private Long id;
    private Long pricelistId;  // for matching in checkRequestedAddOns
    private String contractNumber;
    private String code;  // for display in recommend list
    private String name;  // for display in recommend list

    private Long amount;
    private BigDecimal price;
    private BigDecimal totalAmount;
    
    private Boolean isSelected;  // for book flow: user selection from recommend list
}
