package ru.pt.api.service.schema;

import ru.pt.api.dto.product.LobVar;

import java.util.List;

/**
 * Service for managing contract schema (sections, entities, attributes)
 */
public interface SchemaService {

    void newTenantCreated(Long tid);

    /**
     * Дерево атрибутов схемы договора ({@code mt_attribute_def}).
     *
     * @param tenantCode код тенанта из URL
     * @param contractCode идентификатор документа / модели ({@code document_id})
     */
    List<LobVar> getAttributes(String tenantCode, String contractCode);

    void addAttribute(String tenantCode, String contractCode, LobVar lobVar);

    void updateAttribute(String tenantCode, String contractCode, LobVar lobVar);

    void deleteAttribute(String tenantCode, String contractCode, LobVar lobVar);
}
