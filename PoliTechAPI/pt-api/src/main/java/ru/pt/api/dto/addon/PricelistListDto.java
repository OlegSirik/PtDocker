package ru.pt.api.dto.addon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricelistListDto {
    private Long id;
    private Long providerId;
    private String code;
    private String name;
    private String categoryCode;
    private BigDecimal price;
    private Long amountFree;
    private String status;
}
