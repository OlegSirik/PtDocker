package ru.pt.api.service.product;

import ru.pt.api.dto.llm.LlmAssistRequest;
import ru.pt.api.dto.llm.LlmAssistResponse;
import ru.pt.api.security.AuthenticatedUser;

public interface LlmAssistantService {

    LlmAssistResponse assist(LlmAssistRequest request, AuthenticatedUser user);
}
