package ru.pt.api.dto.addon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricelistDto {
    private Long id;
    private Long providerId;
    private String code;
    private String name;
    private String categoryCode;
    private BigDecimal price;
    private Long amountFree;
    private Long amountBooked;
    private String status;  // ACTIVE, SUSPENDED, DELETED
    private List<AddonProductRef> addonProducts;  // product_id, preconditions from po_addon_products
}
