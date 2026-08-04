package ru.pt.api.service.llm;

import ru.pt.api.dto.llm.TenantLlmApiKeyUpdateRequest;
import ru.pt.api.dto.llm.TenantLlmConfigUpdateRequest;
import ru.pt.api.dto.llm.TenantLlmConfigView;
import ru.pt.api.dto.llm.TenantLlmRuntimeConfig;

public interface TenantLlmConfigService {

    TenantLlmRuntimeConfig resolve(Long tenantId);

    TenantLlmRuntimeConfig resolveForTest(String tenantCode);

    TenantLlmConfigView getConfigView(String tenantCode);

    TenantLlmConfigView saveConfig(String tenantCode, TenantLlmConfigUpdateRequest request);

    TenantLlmConfigView updateApiKey(String tenantCode, TenantLlmApiKeyUpdateRequest request);
}
