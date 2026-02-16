package ru.pt.api.dto.addon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddOnDto {
    private Long id;
    private UUID policyId;
    private Long pricelistId;  // addon_id from po_addon_policies
    private String pricelistCode;  // for display in recommend list
    private String pricelistName;  // for display in recommend list
    private String addOnNumber;
    private String status;  // NEW, BOOKED, PAID
    private Long amount;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private Map<String, Object> policyData;  // JSON data
    private Boolean isSelected;  // for book flow: user selection from recommend list
}
