package ru.pt.api.service.llm;

import ru.pt.api.dto.llm.TenantLlmApiKeyUpdateRequest;
import ru.pt.api.dto.llm.TenantLlmConfigUpdateRequest;
import ru.pt.api.dto.llm.TenantLlmConfigView;
import ru.pt.api.dto.llm.TenantLlmRuntimeConfig;

public interface TenantLlmConfigService {

    TenantLlmRuntimeConfig resolve(Long tenantId);

    TenantLlmConfigView getConfigView(Long tenantId);

    TenantLlmConfigView saveConfig(Long tenantId, TenantLlmConfigUpdateRequest request);

    TenantLlmConfigView updateApiKey(Long tenantId, TenantLlmApiKeyUpdateRequest request);
}
