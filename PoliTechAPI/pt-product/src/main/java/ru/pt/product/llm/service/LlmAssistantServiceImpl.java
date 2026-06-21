package ru.pt.product.llm.service;

import org.springframework.stereotype.Service;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.llm.LlmAssistRequest;
import ru.pt.api.dto.llm.LlmAssistResponse;
import ru.pt.api.dto.llm.LlmCalculatorAssistRequest;
import ru.pt.api.dto.llm.LlmCalculatorAssistResponse;
import ru.pt.api.dto.llm.LlmLobAssistRequest;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.llm.LlmUsage;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.product.LlmAssistantService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;
import ru.pt.product.llm.configuration.LlmProperties;
import ru.pt.product.llm.processor.LlmProcessedResult;
import ru.pt.product.llm.processor.LlmResponseProcessor;
import ru.pt.product.llm.processor.LlmResponseProcessorRegistry;
import ru.pt.product.llm.prompt.PromptAssembler;
import ru.pt.product.llm.prompt.VarsContextFormatter;
import ru.pt.product.llm.provider.LlmCompletionRequest;
import ru.pt.product.llm.provider.LlmCompletionResult;
import ru.pt.product.llm.provider.LlmGateway;
import ru.pt.product.llm.provider.LlmMessage;

import java.util.List;
import java.util.Set;

@Service
public class LlmAssistantServiceImpl implements LlmAssistantService {

    private final ProductService productService;
    private final LobService lobService;
    private final PromptAssembler promptAssembler;
    private final VarsContextFormatter varsContextFormatter;
    private final LlmGateway llmGateway;
    private final LlmResponseProcessorRegistry processorRegistry;
    private final LlmExchangeLogService exchangeLogService;
    private final LlmProperties llmProperties;
    private final LlmCalculatorApplicator calculatorApplicator;

    public LlmAssistantServiceImpl(
            ProductService productService,
            LobService lobService,
            PromptAssembler promptAssembler,
            VarsContextFormatter varsContextFormatter,
            LlmGateway llmGateway,
            LlmResponseProcessorRegistry processorRegistry,
            LlmExchangeLogService exchangeLogService,
            LlmProperties llmProperties,
            LlmCalculatorApplicator calculatorApplicator) {
        this.productService = productService;
        this.lobService = lobService;
        this.promptAssembler = promptAssembler;
        this.varsContextFormatter = varsContextFormatter;
        this.llmGateway = llmGateway;
        this.processorRegistry = processorRegistry;
        this.exchangeLogService = exchangeLogService;
        this.llmProperties = llmProperties;
        this.calculatorApplicator = calculatorApplicator;
    }

    @Override
    public LlmAssistResponse assist(LlmAssistRequest request, AuthenticatedUser user) {
        validateRequest(request);
        ProductVersionModel product = productService.getVersion(
                user.getTenantId(),
                request.getProductId(),
                request.getVersionNo());

        List<LlmMessage> messages = promptAssembler.assemble(
                request.getTaskType(),
                request.getUserMessage(),
                product,
                user.getTenantId());

        Set<String> knownVarCodes = varsContextFormatter.varCodes(product.getVars());
        return completeAndProcess(request, product, messages, knownVarCodes, user);
    }

    @Override
    public LlmAssistResponse assistLob(LlmLobAssistRequest request, AuthenticatedUser user) {
        validateLobRequest(request);
        String lobCode = request.getLobCode().trim();
        LobModel lob = lobService.getByCode(user.getTenantId(), lobCode);
        if (lob == null) {
            throw new NotFoundException("LOB not found: " + lobCode);
        }

        List<PvVar> vars = lobVarsToPvVars(lob);
        List<LlmMessage> messages = promptAssembler.assembleForLob(
                request.getTaskType(),
                request.getUserMessage(),
                lob.getMpCode(),
                lob.getMpName(),
                vars,
                user.getTenantId());

        Set<String> knownVarCodes = varsContextFormatter.varCodes(vars);
        return completeAndProcessLob(request, lob, messages, knownVarCodes, vars, user);
    }

    @Override
    public LlmCalculatorAssistResponse assistCalculator(LlmCalculatorAssistRequest request, AuthenticatedUser user) {
        validateCalculatorRequest(request);
        CalculatorModel calculator = request.getCurrentCalculator();
        String userMessage = resolveCalculatorUserMessage(request);

        ProductVersionModel product = productService.getVersion(
                user.getTenantId(),
                calculator.getProductId(),
                calculator.getVersionNo());

        List<LlmMessage> messages = promptAssembler.assembleCalculator(userMessage, calculator);

        List<PvVar> calculatorVars = calculator.getVars() != null ? calculator.getVars() : List.of();
        Set<String> knownVarCodes = varsContextFormatter.varCodes(calculatorVars);
        LlmAssistRequest logRequest = toAssistRequest(request, userMessage);

        LlmAssistResponse llmResponse = completeAndProcess(
                logRequest,
                product,
                messages,
                knownVarCodes,
                user);

        LlmCalculatorAssistResponse response = new LlmCalculatorAssistResponse();
        response.setSuccess(llmResponse.isSuccess());

        if (llmResponse.isSuccess()) {
            if (!(llmResponse.getResult() instanceof CalculatorModel fromLlm)) {
                response.setSuccess(false);
                response.setMessage("В ответе нет данных калькулятора");
                return response;
            }

            calculatorApplicator.apply(calculator, fromLlm);
            response.setCalculator(calculator);
        } else if (llmResponse.getErrors() != null && !llmResponse.getErrors().isEmpty()) {
            response.setMessage(String.join("; ", llmResponse.getErrors()));
        } else {
            response.setMessage("LLM вернул ошибку");
        }

        return response;
    }

    private LlmAssistResponse completeAndProcess(
            LlmAssistRequest request,
            ProductVersionModel product,
            List<LlmMessage> messages,
            Set<String> knownVarCodes,
            AuthenticatedUser user) {
        String providerCode = resolveProviderCode(request);
        LlmCompletionResult completion;
        try {
            completion = llmGateway.complete(
                    new LlmCompletionRequest(messages, null, 0.1, 4096, true),
                    request.getProviderCode(),
                    request.getModel());
        } catch (RuntimeException ex) {
            exchangeLogService.logFailure(request, product, messages, providerCode, user, ex.getMessage());
            throw ex;
        }

        LlmResponseProcessor processor = processorRegistry.get(request.getTaskType());
        LlmProcessedResult processed = processor.process(
                completion.content(),
                knownVarCodes,
                product.getVars());

        LlmAssistResponse response = new LlmAssistResponse();
        response.setTaskType(request.getTaskType());
        response.setRawContent(completion.content());
        response.setSuccess(processed.isSuccess());
        response.setResult(processed.getResult());
        response.setWarnings(processed.getWarnings());
        response.setErrors(processed.getErrors());

        LlmUsage usage = new LlmUsage();
        usage.setProvider(providerCode);
        usage.setModel(completion.model());
        usage.setPromptTokens(completion.promptTokens());
        usage.setCompletionTokens(completion.completionTokens());
        usage.setLatencyMs(completion.latencyMs());
        response.setUsage(usage);

        exchangeLogService.logExchange(request, product, messages, response, completion, providerCode, user);
        return response;
    }

    private LlmAssistResponse completeAndProcessLob(
            LlmLobAssistRequest request,
            LobModel lob,
            List<LlmMessage> messages,
            Set<String> knownVarCodes,
            List<PvVar> vars,
            AuthenticatedUser user) {
        String providerCode = resolveLobProviderCode(request);
        LlmCompletionResult completion;
        try {
            completion = llmGateway.complete(
                    new LlmCompletionRequest(messages, null, 0.1, 4096, true),
                    request.getProviderCode(),
                    request.getModel());
        } catch (RuntimeException ex) {
            exchangeLogService.logLobFailure(request, lob, messages, providerCode, user, ex.getMessage());
            throw ex;
        }

        LlmResponseProcessor processor = processorRegistry.get(request.getTaskType());
        LlmProcessedResult processed = processor.process(
                completion.content(),
                knownVarCodes,
                vars);

        LlmAssistResponse response = new LlmAssistResponse();
        response.setTaskType(request.getTaskType());
        response.setRawContent(completion.content());
        response.setSuccess(processed.isSuccess());
        response.setResult(processed.getResult());
        response.setWarnings(processed.getWarnings());
        response.setErrors(processed.getErrors());

        LlmUsage usage = new LlmUsage();
        usage.setProvider(providerCode);
        usage.setModel(completion.model());
        usage.setPromptTokens(completion.promptTokens());
        usage.setCompletionTokens(completion.completionTokens());
        usage.setLatencyMs(completion.latencyMs());
        response.setUsage(usage);

        exchangeLogService.logLobExchange(request, lob, messages, response, completion, providerCode, user);
        return response;
    }

    private List<PvVar> lobVarsToPvVars(LobModel lob) {
        if (lob.getMpVars() == null || lob.getMpVars().isEmpty()) {
            return List.of();
        }
        return lob.getMpVars().stream()
                .filter(v -> v != null && !v.getIsDeleted())
                .map(PvVar::new)
                .toList();
    }

    private LlmAssistRequest toAssistRequest(LlmCalculatorAssistRequest request, String userMessage) {
        CalculatorModel calculator = request.getCurrentCalculator();
        LlmAssistRequest assistRequest = new LlmAssistRequest();
        assistRequest.setTaskType(request.getTaskType() != null ? request.getTaskType() : LlmTaskType.CALCULATOR);
        assistRequest.setUserMessage(userMessage);
        assistRequest.setProductId(firstNonNull(request.getProductId(), calculator.getProductId()));
        assistRequest.setVersionNo(firstNonNull(request.getVersionNo(), calculator.getVersionNo()));
        assistRequest.setPackageNo(firstNonBlank(request.getPackageNo(), calculator.getPackageNo()));
        assistRequest.setProviderCode(request.getProviderCode());
        assistRequest.setModel(request.getModel());
        return assistRequest;
    }

    private String resolveCalculatorUserMessage(LlmCalculatorAssistRequest request) {
        if (request.getUserMessage() != null && !request.getUserMessage().isBlank()) {
            return request.getUserMessage().trim();
        }
        CalculatorModel calculator = request.getCurrentCalculator();
        if (calculator != null && calculator.getLlmText() != null && !calculator.getLlmText().isBlank()) {
            return calculator.getLlmText().trim();
        }
        throw new BadRequestException("userMessage is required");
    }

    private static Long firstNonNull(Long primary, Long fallback) {
        return primary != null ? primary : fallback;
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }

    private String resolveProviderCode(LlmAssistRequest request) {
        if (request.getProviderCode() != null && !request.getProviderCode().isBlank()) {
            return request.getProviderCode();
        }
        return llmProperties.getDefaultProvider();
    }

    private String resolveLobProviderCode(LlmLobAssistRequest request) {
        if (request.getProviderCode() != null && !request.getProviderCode().isBlank()) {
            return request.getProviderCode();
        }
        return llmProperties.getDefaultProvider();
    }

    private void validateRequest(LlmAssistRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (request.getTaskType() == null) {
            throw new BadRequestException("taskType is required");
        }
        if (request.getUserMessage() == null || request.getUserMessage().isBlank()) {
            throw new BadRequestException("userMessage is required");
        }
        if (request.getProductId() == null) {
            throw new BadRequestException("productId is required");
        }
        if (request.getVersionNo() == null) {
            throw new BadRequestException("versionNo is required");
        }
    }

    private void validateLobRequest(LlmLobAssistRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (request.getTaskType() == null) {
            throw new BadRequestException("taskType is required");
        }
        if (request.getTaskType() != LlmTaskType.RULE) {
            throw new BadRequestException("Only RULE taskType is supported for LOB assist");
        }
        if (request.getUserMessage() == null || request.getUserMessage().isBlank()) {
            throw new BadRequestException("userMessage is required");
        }
        if (request.getLobCode() == null || request.getLobCode().isBlank()) {
            throw new BadRequestException("lobCode is required");
        }
    }

    private void validateCalculatorRequest(LlmCalculatorAssistRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (request.getCurrentCalculator() == null) {
            throw new BadRequestException("currentCalculator is required");
        }
        CalculatorModel calculator = request.getCurrentCalculator();
        if (firstNonNull(request.getProductId(), calculator.getProductId()) == null) {
            throw new BadRequestException("productId is required");
        }
        if (firstNonNull(request.getVersionNo(), calculator.getVersionNo()) == null) {
            throw new BadRequestException("versionNo is required");
        }
        if (firstNonBlank(request.getPackageNo(), calculator.getPackageNo()) == null
                || firstNonBlank(request.getPackageNo(), calculator.getPackageNo()).isBlank()) {
            throw new BadRequestException("packageNo is required");
        }
        String userMessage = request.getUserMessage();
        String llmText = calculator.getLlmText();
        boolean hasMessage = userMessage != null && !userMessage.isBlank();
        boolean hasLlmText = llmText != null && !llmText.isBlank();
        if (!hasMessage && !hasLlmText) {
            throw new BadRequestException("userMessage is required");
        }
    }
}
