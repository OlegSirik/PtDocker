package ru.pt.api.service.addon;

import ru.pt.api.dto.addon.ProviderDto;
import ru.pt.api.dto.addon.ProviderListDto;

import java.util.List;

public interface AddOnProviderService {

    ProviderDto createProvider(ProviderDto cmd);

    ProviderDto updateProvider(ProviderDto cmd);

    void suspendProvider(Long id);

    List<ProviderListDto> getProviders();

    ProviderDto getProvider(Long id);
}
