package ru.pt.api.mapper;

import ru.pt.api.dto.process.Insurer;
import ru.pt.api.dto.product.InsuranceCompanyDto;

/**
 * Сборка {@link Insurer} для полиса из админского {@link InsuranceCompanyDto}.
 */
public final class InsurerMapper {

    private InsurerMapper() {
    }

    public static Insurer fromInsuranceCompany(InsuranceCompanyDto dto) {
        if (dto == null) {
            return null;
        }
        Insurer insurer = new Insurer();
 
        insurer.setCode(dto.getCode());
        insurer.setName( dto.getName());
        insurer.setContractText(dto.getContractText());
        insurer.setContractRepresentative(dto.getContractRepresentative());
        return insurer;
    }

   

}
