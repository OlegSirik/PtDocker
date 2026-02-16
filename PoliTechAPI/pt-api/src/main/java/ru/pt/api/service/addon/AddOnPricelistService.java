package ru.pt.api.service.addon;

import ru.pt.api.dto.addon.PricelistDto;
import ru.pt.api.dto.addon.PricelistListDto;

import java.util.List;

public interface AddOnPricelistService {

    PricelistDto createPricelist(PricelistDto cmd);

    PricelistDto updatePricelist(PricelistDto cmd);

    void deletePricelist(Long id);

    void suspendPricelist(Long id);

    List<PricelistListDto> getPricelists(Long providerId);

    PricelistDto getPricelist(Long id);
}
