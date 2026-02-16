package ru.pt.api.service.addon;

import ru.pt.api.dto.addon.PolicyAddOnDto;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.domain.model.VariableContext;

import java.util.List;
import java.util.UUID;

public interface PolicyAddOnService {


    /*
    Проверить запрашиваемый список опций
    Если все Ок, то вернуть его + еще опции по продукту
    Если нет то эксепшен.
     */
    List<PolicyAddOnDto> checkRequestedAddOns(ProductVersionModel product, VariableContext ctx, List<PolicyAddOnDto> requestedList);

    List<PolicyAddOnDto> recommendAddOn(ProductVersionModel product, VariableContext ctx);

    void bookAddOn(UUID policyId, Long addOnId, ProductVersionModel product, VariableContext ctx);

    void markPaid(UUID policyId, Long addOnId);

    List<PolicyAddOnDto> getPolicyAddOns(UUID policyId);
}
