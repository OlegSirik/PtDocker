package ru.pt.api.service.policy;

import ru.pt.api.dto.policy.StdPolicy;

/**
 * Маппинг JSON ↔ {@link StdPolicy} для конкретного формата документа.
 */
public interface StdPolicyMapper {

    String getFormat();

    StdPolicy fromJson(String json);

    String toJson(StdPolicy policy);
}
