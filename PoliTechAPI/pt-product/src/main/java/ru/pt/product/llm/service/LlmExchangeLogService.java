package ru.pt.product.llm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.llm.LlmAssistRequest;
import ru.pt.api.dto.llm.LlmAssistResponse;
import ru.pt.api.dto.llm.LlmLobAssistRequest;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.product.entity.LlmExchangeEntity;
import ru.pt.product.llm.provider.LlmCompletionResult;
import ru.pt.product.llm.provider.LlmMessage;
import ru.pt.product.repository.LlmExchangeRepository;

import java.util.List;

@Service
public class LlmExchangeLogService {

    private static final Logger log = LoggerFactory.getLogger(LlmExchangeLogService.class);

    private final LlmExchangeRepository repository;
    private final ObjectMapper objectMapper;

    public LlmExchangeLogService(LlmExchangeRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void logExchange(
            LlmAssistRequest request,
            ProductVersionModel product,
            List<LlmMessage> messages,
            LlmAssistResponse response,
            LlmCompletionResult completion,
            String providerCode,
            AuthenticatedUser user) {
        try {
            LlmExchangeEntity entity = new LlmExchangeEntity();
            entity.setTid(user.getTenantId());
            entity.setUserAccountId(user.getId());
            entity.setTaskType(request.getTaskType().name());
            entity.setProductId(request.getProductId());
            entity.setVersionNo(request.getVersionNo());
            entity.setProductCode(product.getCode());
            entity.setProviderCode(providerCode);
            entity.setRequest(buildRequestJson(request, product, messages));
            if (completion != null) {
                entity.setModel(completion.model());
                entity.setPromptTokens(completion.promptTokens());
                entity.setCompletionTokens(completion.completionTokens());
                entity.setLatencyMs(completion.latencyMs());
            }
            if (response != null) {
                entity.setSuccess(response.isSuccess());
                entity.setStatus(resolveStatus(response));
                entity.setResponse(buildResponseJson(response));
            } else {
                entity.setSuccess(false);
                entity.setStatus("PROVIDER_ERROR");
            }
            repository.save(entity);
        } catch (Exception ex) {
            log.warn("Failed to persist llm_exchange log: {}", ex.getMessage());
        }
    }

    @Transactional
    public void logFailure(
            LlmAssistRequest request,
            ProductVersionModel product,
            List<LlmMessage> messages,
            String providerCode,
            AuthenticatedUser user,
            String errorMessage) {
        try {
            LlmExchangeEntity entity = new LlmExchangeEntity();
            entity.setTid(user.getTenantId());
            entity.setUserAccountId(user.getId());
            entity.setTaskType(request.getTaskType().name());
            entity.setProductId(request.getProductId());
            entity.setVersionNo(request.getVersionNo());
            entity.setProductCode(product.getCode());
            entity.setProviderCode(providerCode);
            entity.setRequest(buildRequestJson(request, product, messages));
            entity.setSuccess(false);
            entity.setStatus("PROVIDER_ERROR");
            entity.setResponse(toJson(objectMapper.createObjectNode().put("error", errorMessage)));
            repository.save(entity);
        } catch (Exception ex) {
            log.warn("Failed to persist llm_exchange failure log: {}", ex.getMessage());
        }
    }

    @Transactional
    public void logLobExchange(
            LlmLobAssistRequest request,
            LobModel lob,
            List<LlmMessage> messages,
            LlmAssistResponse response,
            LlmCompletionResult completion,
            String providerCode,
            AuthenticatedUser user) {
        try {
            LlmExchangeEntity entity = new LlmExchangeEntity();
            entity.setTid(user.getTenantId());
            entity.setUserAccountId(user.getId());
            entity.setTaskType(request.getTaskType().name());
            entity.setProductCode(lob.getMpCode());
            entity.setProviderCode(providerCode);
            entity.setRequest(buildLobRequestJson(request, lob, messages));
            if (completion != null) {
                entity.setModel(completion.model());
                entity.setPromptTokens(completion.promptTokens());
                entity.setCompletionTokens(completion.completionTokens());
                entity.setLatencyMs(completion.latencyMs());
            }
            if (response != null) {
                entity.setSuccess(response.isSuccess());
                entity.setStatus(resolveStatus(response));
                entity.setResponse(buildResponseJson(response));
            } else {
                entity.setSuccess(false);
                entity.setStatus("PROVIDER_ERROR");
            }
            repository.save(entity);
        } catch (Exception ex) {
            log.warn("Failed to persist llm_exchange lob log: {}", ex.getMessage());
        }
    }

    @Transactional
    public void logLobFailure(
            LlmLobAssistRequest request,
            LobModel lob,
            List<LlmMessage> messages,
            String providerCode,
            AuthenticatedUser user,
            String errorMessage) {
        try {
            LlmExchangeEntity entity = new LlmExchangeEntity();
            entity.setTid(user.getTenantId());
            entity.setUserAccountId(user.getId());
            entity.setTaskType(request.getTaskType().name());
            entity.setProductCode(lob.getMpCode());
            entity.setProviderCode(providerCode);
            entity.setRequest(buildLobRequestJson(request, lob, messages));
            entity.setSuccess(false);
            entity.setStatus("PROVIDER_ERROR");
            entity.setResponse(toJson(objectMapper.createObjectNode().put("error", errorMessage)));
            repository.save(entity);
        } catch (Exception ex) {
            log.warn("Failed to persist llm_exchange lob failure log: {}", ex.getMessage());
        }
    }

    private String buildLobRequestJson(LlmLobAssistRequest request, LobModel lob, List<LlmMessage> messages) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("taskType", request.getTaskType().name());
        root.put("userMessage", request.getUserMessage());
        root.put("lobCode", lob.getMpCode());
        root.put("lobName", lob.getMpName());
        ArrayNode msgArray = root.putArray("messages");
        for (LlmMessage message : messages) {
            ObjectNode msg = msgArray.addObject();
            msg.put("role", message.role());
            msg.put("content", message.content());
        }
        return toJson(root);
    }

    private String buildRequestJson(LlmAssistRequest request, ProductVersionModel product, List<LlmMessage> messages) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("taskType", request.getTaskType().name());
        root.put("userMessage", request.getUserMessage());
        root.put("productId", request.getProductId());
        root.put("versionNo", request.getVersionNo());
        if (request.getPackageNo() != null) {
            root.put("packageNo", request.getPackageNo());
        }
        root.put("productCode", product.getCode());
        root.put("lob", product.getLob());
        ArrayNode msgArray = root.putArray("messages");
        for (LlmMessage message : messages) {
            ObjectNode msg = msgArray.addObject();
            msg.put("role", message.role());
            msg.put("content", message.content());
        }
        return toJson(root);
    }

    private String buildResponseJson(LlmAssistResponse response) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("success", response.isSuccess());
        root.put("rawContent", response.getRawContent());
        root.set("result", objectMapper.valueToTree(response.getResult()));
        root.set("warnings", objectMapper.valueToTree(response.getWarnings()));
        root.set("errors", objectMapper.valueToTree(response.getErrors()));
        if (response.getUsage() != null) {
            root.set("usage", objectMapper.valueToTree(response.getUsage()));
        }
        return toJson(root);
    }

    private static String resolveStatus(LlmAssistResponse response) {
        if (response.isSuccess()) {
            return "OK";
        }
        if (response.getErrors() != null && response.getErrors().stream()
                .anyMatch(e -> e != null && e.startsWith("CEL compile"))) {
            return "CEL_ERROR";
        }
        if (response.getErrors() != null && response.getErrors().stream()
                .anyMatch(e -> e != null && e.startsWith("Parse error"))) {
            return "PARSE_ERROR";
        }
        return "ERROR";
    }

    private String toJson(ObjectNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
