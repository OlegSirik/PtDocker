package ru.pt.product.llm.service;

import org.springframework.stereotype.Service;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.llm.LlmAssistRequest;
import ru.pt.api.dto.llm.LlmAssistResponse;
import ru.pt.api.dto.llm.LlmCalculatorAssistRequest;
import ru.pt.api.dto.llm.LlmCalculatorAssistResponse;
import ru.pt.api.dto.llm.LlmCalculatorDraft;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.llm.LlmUsage;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.product.LlmAssistantService;
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
    private final PromptAssembler promptAssembler;
    private final VarsContextFormatter varsContextFormatter;
    private final LlmGateway llmGateway;
    private final LlmResponseProcessorRegistry processorRegistry;
    private final LlmExchangeLogService exchangeLogService;
    private final LlmProperties llmProperties;
    private final LlmCalculatorApplicator calculatorApplicator;

    public LlmAssistantServiceImpl(
            ProductService productService,
            PromptAssembler promptAssembler,
            VarsContextFormatter varsContextFormatter,
            LlmGateway llmGateway,
            LlmResponseProcessorRegistry processorRegistry,
            LlmExchangeLogService exchangeLogService,
            LlmProperties llmProperties,
            LlmCalculatorApplicator calculatorApplicator) {
        this.productService = productService;
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
                product);

        Set<String> knownVarCodes = varsContextFormatter.varCodes(product.getVars());
        return completeAndProcess(request, product, messages, knownVarCodes, user);
    }

    @Override
    public LlmCalculatorAssistResponse assistCalculator(LlmCalculatorAssistRequest request, AuthenticatedUser user) {
        validateCalculatorRequest(request);
        CalculatorModel calculator = request.getCalculator();

        ProductVersionModel product = productService.getVersion(
                user.getTenantId(),
                calculator.getProductId(),
                calculator.getVersionNo());

        List<PvVar> calculatorVars = calculator.getVars() != null ? calculator.getVars() : List.of();

        List<LlmMessage> messages = promptAssembler.assembleCalculator(
                request.getUserMessage(),
                product,
                calculator.getPackageNo(),
                calculatorVars);

        Set<String> knownVarCodes = varsContextFormatter.mergedVarCodes(product.getVars(), calculatorVars);
        LlmAssistRequest logRequest = toAssistRequest(request);

        LlmAssistResponse llmResponse = completeAndProcess(
                logRequest,
                product,
                messages,
                knownVarCodes,
                user);

        LlmCalculatorAssistResponse response = new LlmCalculatorAssistResponse();
        response.setSuccess(llmResponse.isSuccess());

        if (llmResponse.isSuccess()) {
            if (!(llmResponse.getResult() instanceof LlmCalculatorDraft draft)) {
                response.setSuccess(false);
                response.setMessage("В ответе нет данных калькулятора");
                return response;
            }
            if ((draft.getFormulas() == null || draft.getFormulas().isEmpty())
                    && (draft.getCoefficients() == null || draft.getCoefficients().isEmpty())) {
                response.setSuccess(false);
                response.setMessage("В ответе нет формул и коэффициентов");
                return response;
            }

            calculatorApplicator.apply(calculator, draft);
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

    private LlmAssistRequest toAssistRequest(LlmCalculatorAssistRequest request) {
        CalculatorModel calculator = request.getCalculator();
        LlmAssistRequest assistRequest = new LlmAssistRequest();
        assistRequest.setTaskType(LlmTaskType.CALCULATOR);
        assistRequest.setUserMessage(request.getUserMessage());
        assistRequest.setProductId(calculator.getProductId());
        assistRequest.setVersionNo(calculator.getVersionNo());
        assistRequest.setPackageNo(calculator.getPackageNo());
        assistRequest.setProviderCode(request.getProviderCode());
        assistRequest.setModel(request.getModel());
        return assistRequest;
    }

    private String resolveProviderCode(LlmAssistRequest request) {
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

    private void validateCalculatorRequest(LlmCalculatorAssistRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (request.getUserMessage() == null || request.getUserMessage().isBlank()) {
            throw new BadRequestException("userMessage is required");
        }
        if (request.getCalculator() == null) {
            throw new BadRequestException("calculator is required");
        }
        CalculatorModel calculator = request.getCalculator();
        if (calculator.getProductId() == null) {
            throw new BadRequestException("calculator.productId is required");
        }
        if (calculator.getVersionNo() == null) {
            throw new BadRequestException("calculator.versionNo is required");
        }
        if (calculator.getPackageNo() == null || calculator.getPackageNo().isBlank()) {
            throw new BadRequestException("calculator.packageNo is required");
        }
    }
}
