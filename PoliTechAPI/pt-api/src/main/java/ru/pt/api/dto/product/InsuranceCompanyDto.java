package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Страховая компания: основные поля + реквизиты в {@code other_props} на стороне БД (маппер).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InsuranceCompanyDto {

    private Long id;
    private Long tid;
    private String code;
    private String name;
    /** active / suspended */
    private String status;

    private String shortName;
    private String fullName;
    private String egr;
    private String postalAddress;
    private String legalAddress;
    private String phone;
    private String mail;
    private String inn;
    private String kpp;
    private String okpo;
    private String ogrn;
    private String account;
    private String bank;
    private String bic;
    private String corrAccount;
    private String contractText;
    private String contractRepresentative; // ФИО представителя страховой компании для печати внизу договора
}
