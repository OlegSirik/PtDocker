package ru.pt.api.service.product;

import ru.pt.api.dto.llm.LlmAssistRequest;
import ru.pt.api.dto.llm.LlmAssistResponse;
import ru.pt.api.dto.llm.LlmCalculatorAssistRequest;
import ru.pt.api.dto.llm.LlmCalculatorAssistResponse;
import ru.pt.api.dto.llm.LlmLobAssistRequest;
import ru.pt.api.security.AuthenticatedUser;

public interface LlmAssistantService {

    LlmAssistResponse assist(LlmAssistRequest request, AuthenticatedUser user);

    LlmAssistResponse assistLob(LlmLobAssistRequest request, AuthenticatedUser user);

    LlmCalculatorAssistResponse assistCalculator(LlmCalculatorAssistRequest request, AuthenticatedUser user);
}
