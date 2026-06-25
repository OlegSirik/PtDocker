package ru.pt.api.service.policy;

import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.policy.StdPolicy;

public interface StdPolicyFactory {

    StdPolicy build(String format, String json);

    default StdPolicy fromJson(String format, String json) {
        return build(format, json);
    }

    /**
     * Восстановить {@link StdPolicy} из хранилища.
     * Формат берётся из {@link PolicyData#resolveDocumentFormat()} (колонка policy_index.document_format).
     */
    StdPolicy fromStorage(PolicyData policyData);
}
