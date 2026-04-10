package ru.pt.api.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ru.pt.api.dto.refs.RecordStatus;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Long id;
    private String lob;
    private String code;
    private String name;
    private Long prodVersionNo;
    private Long devVersionNo;
    /** pt_insurance_company.id, может быть null */
    private Long insCompanyId;
    private RecordStatus recordStatus;
}
