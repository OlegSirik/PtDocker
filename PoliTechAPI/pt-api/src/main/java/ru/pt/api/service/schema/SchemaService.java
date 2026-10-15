package ru.pt.api.service.schema;

import ru.pt.api.dto.product.ContractModelDto;
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
     * Список моделей договора тенанта ({@code mt_contract_model}).
     */
    List<ContractModelDto> listContractModels(Long tenantId);

    /**
     * Дерево атрибутов схемы договора ({@code mt_attribute_def}).
     *
     * @param tenantCode код тенанта из URL
     * @param contractCode идентификатор документа / модели ({@code document_id})
     */
    List<LobVar> getAttributes(Long tenantId, String contractCode);

    /**
     * JSON-скелет по листьям схемы: структура и имена полей из нормализованного {@code var_path}
     * (JsonPath; {@code [?()]} / {@code [*]} → {@code [0]}), значения-заглушки по {@code var_data_type}.
     * {@code var_code} только идентифицирует атрибут, в ключи JSON не попадает.
     */
    String getAttributesMetadataJson(Long tenantId, String contractCode);

    /**
     * Как {@link #getAttributesMetadataJson(Long, String)}, но только листья {@code IN} с
     * {@code isSystem} или {@code var_path} из {@code varValues}. Ключи карты — JsonPath
     * ({@code var_path}), не {@code var_code}; размещение в JSON — по тому же path.
     */
    String getAttributesMetadataJson(Long tenantId, String contractCode, Map<String, String> varValues);

    /**
     * Создать атрибут в схеме; копия добавляется в {@code mpVars} всех активных LOB тенанта
     * ({@code pt_lobs}) с {@code isDeleted = true}.
     */
    void addAttribute(Long tenantId, String contractCode, LobVar lobVar);

    void updateAttribute(Long tenantId, String contractCode, LobVar lobVar);

    void deleteAttribute(Long tenantId, String contractCode, LobVar lobVar);

    /**
     * Следующее значение последовательности {@code mt_attribute_def_seq}.
     */
    Long nextId();
}
