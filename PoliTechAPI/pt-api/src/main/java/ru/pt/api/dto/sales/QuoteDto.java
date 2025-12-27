package ru.pt.api.dto.sales;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * DTO for quote information
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuoteDto(
    String id, // uuid
    String draftId, // uuid
    String policyNr,
    String productCode,
    String insCompany,
    ZonedDateTime createDate, // Date | string
    ZonedDateTime issueDate, // Date | string
    String issueTimezone,
    ZonedDateTime paymentDate, // Date | string
    ZonedDateTime startDate, // Date | string
    ZonedDateTime endDate, // Date | string
    String policyStatus,
    String phDigest,
    String ioDigest,
    String premium,
    String agentDigest,
    String agentKvPrecent,
    String agentKvAmount,
    Boolean comand1,
    Boolean comand2,
    Boolean comand3,
    Boolean comand4,
    Boolean comand5,
    Boolean comand6,
    Boolean comand7,
    Boolean comand8,
    Boolean comand9
) {}
