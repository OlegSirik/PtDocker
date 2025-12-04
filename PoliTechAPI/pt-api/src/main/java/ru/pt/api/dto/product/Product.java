package ru.pt.api.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Integer id;
    private String lob;
    private String code;
    private String name;
    private Integer prodVersionNo;
    private Integer devVersionNo;
    private boolean isDeleted = false;
}
