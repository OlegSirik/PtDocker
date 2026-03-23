package ru.pt.api.service.product;

import ru.pt.api.dto.product.InsuranceCompanyDto;

import java.util.List;

public interface InsCompanyService {

    InsuranceCompanyDto create(InsuranceCompanyDto dto);

    InsuranceCompanyDto update(InsuranceCompanyDto dto);

    void delete(Long id);

    InsuranceCompanyDto get(Long id);

    List<InsuranceCompanyDto> getAll();
}
