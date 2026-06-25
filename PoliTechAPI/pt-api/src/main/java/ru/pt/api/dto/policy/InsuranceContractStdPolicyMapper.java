package ru.pt.api.dto.policy;

import ru.pt.api.service.policy.StdPolicyMapper;

public final class InsuranceContractStdPolicyMapper implements StdPolicyMapper {

    @Override
    public String getFormat() {
        return StdPolicyFormat.INSURANCE_CONTRACT;
    }

    @Override
    public StdPolicy fromJson(String json) {
        return InsuranceContractPolicy.fromJson(json);
    }

    @Override
    public String toJson(StdPolicy policy) {
        return policy.toJson();
    }
}
