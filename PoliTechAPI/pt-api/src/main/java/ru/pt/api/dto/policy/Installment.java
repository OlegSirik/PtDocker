package ru.pt.api.dto.policy;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Installment(
        Long installmentNr,
        LocalDate dueDate,
        BigDecimal amount
) {
}
