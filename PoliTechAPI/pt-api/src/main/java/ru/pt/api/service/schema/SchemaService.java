package ru.pt.api.service.schema;

import ru.pt.api.dto.schema.AttributeDefDto;
import ru.pt.api.dto.schema.EntityDefDto;
import ru.pt.api.dto.schema.SectionDto;

import java.util.List;

/**
 * Service for managing contract schema (sections, entities, attributes)
 */
public interface SchemaService {

    void newTenantCreated(Long tid);
    // Sections
    List<SectionDto> getSections(Long tid, String contractCode);
    SectionDto createSection(Long tid, String contractCode, SectionDto dto);
    SectionDto updateSection(Long tid, String contractCode, String code, SectionDto dto);
    void deleteSection(Long tid, String contractCode, String code);

    // Entities
    List<EntityDefDto> getEntities(Long tid, String contractCode, String sectionCode);
    EntityDefDto createEntity(Long tid, String contractCode, String sectionCode, EntityDefDto dto);
    EntityDefDto updateEntity(Long tid, String contractCode, String sectionCode, String code, EntityDefDto dto);
    void deleteEntity(Long tid, String contractCode, String sectionCode, String code);

    // Attributes
    List<AttributeDefDto> getAttributes(Long tid, String contractCode, String sectionCode, String entityCode);
    AttributeDefDto createAttribute(Long tid, String contractCode, String sectionCode, String entityCode, AttributeDefDto dto);
    AttributeDefDto updateAttribute(Long tid, String contractCode, String sectionCode, String entityCode, String code, AttributeDefDto dto);
    void deleteAttribute(Long tid, String contractCode, String sectionCode, String entityCode, String code);
}
