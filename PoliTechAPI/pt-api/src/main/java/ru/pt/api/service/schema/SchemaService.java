package ru.pt.api.service.schema;

import ru.pt.api.dto.product.LobVar;

import java.util.List;
import java.util.Map;

/**
 * Service for managing contract schema (sections, entities, attributes)
 */
public interface SchemaService {

    public static final String INSURANCE_CONTRACT = "INSURANCE_CONTRACT";
    
    void newTenantCreated(Long tid);

    /**
     * Дерево атрибутов схемы договора ({@code mt_attribute_def}).
     *
     * @param tenantCode код тенанта из URL
     * @param contractCode идентификатор документа / модели ({@code document_id})
     */
    List<LobVar> getAttributes(Long tenantId, String contractCode);

    /**
     * JSON template from attribute tree. Rows with {@code parent_id == null} are technical roots and
     * are omitted; top-level JSON fields are their direct children, then by {@code parent_id}.
     * Field name: {@code code} or else {@code varCode}; leaf {@code ""}; else nested structure.
     */
    String getAttributesMetadataJson(Long tenantId, String contractCode);

    /**
     * Как {@link #getAttributesMetadataJson(String, String)}, но в дереве остаются только узлы с
     * {@code isSystem == true} или с {@code var_code}, присутствующим в {@code varValues}.
     * Для ключей из {@code varValues} подставляется соответствующее значение; для системных полей
     * вне карты — {@code var_value} из схемы. Пустые {@code OBJECT}/{@code ARRAY} после фильтрации
     * из результата исключаются.
     */
    String getAttributesMetadataJson(Long tenantId, String contractCode, Map<String, String> varValues);

    void addAttribute(Long tenantId, String contractCode, LobVar lobVar);

    void updateAttribute(Long tenantId, String contractCode, LobVar lobVar);

    void deleteAttribute(Long tenantId, String contractCode, LobVar lobVar);

    /**
     * Следующее значение последовательности {@code mt_attribute_def_seq}.
     */
    Long nextId();
}
