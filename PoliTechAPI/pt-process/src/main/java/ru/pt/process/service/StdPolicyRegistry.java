package ru.pt.process.service;

import org.springframework.stereotype.Service;

import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.errors.ErrorModel;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.api.dto.policy.StdPolicy;
import ru.pt.api.service.policy.StdPolicyFactory;
import ru.pt.api.service.policy.StdPolicyMapper;

import java.util.List;

@Service
public class StdPolicyRegistry implements StdPolicyFactory {

    private final List<StdPolicyMapper> mappers;

    public StdPolicyRegistry(List<StdPolicyMapper> mappers) {
        if (mappers == null || mappers.isEmpty()) {
            throw new IllegalStateException("At least one StdPolicyMapper bean is required");
        }
        this.mappers = List.copyOf(mappers);
    }

    @Override
    public StdPolicy build(String format, String json) {
        return findMapper(format).fromJson(json);
    }

    @Override
    public StdPolicy fromJson(String format, String json) {
        return build(format, json);
    }

    @Override
    public StdPolicy fromStorage(PolicyData policyData) {
        return build(policyData.resolveDocumentFormat(), policyData.getPolicy());
    }

    private StdPolicyMapper findMapper(String format) {
        return mappers.stream()
                .filter(m -> m.getFormat().equals(format))
                .findFirst()
                .orElseThrow(() -> {
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                            422,
                            "Unsupported policy format: " + format,
                            ErrorConstants.DOMAIN_POLICY,
                            ErrorConstants.REASON_INVALID_FORMAT,
                            "documentFormat"
                    );
                    return new UnprocessableEntityException(errorModel);
                });
    }
}
