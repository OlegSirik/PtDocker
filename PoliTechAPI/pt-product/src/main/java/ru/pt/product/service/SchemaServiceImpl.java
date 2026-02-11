package ru.pt.product.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.schema.AttributeDefDto;
import ru.pt.api.dto.schema.EntityDefDto;
import ru.pt.api.dto.schema.SectionDto;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.schema.SchemaService;
import ru.pt.product.entity.AttributeDefEntity;
import ru.pt.product.entity.ContractModelEntity;
import ru.pt.product.entity.ContractSectionEntity;
import ru.pt.product.entity.EntityDefEntity;
import ru.pt.product.repository.AttributeDefRepository;
import ru.pt.product.repository.ContractModelRepository;
import ru.pt.product.repository.ContractSectionRepository;
import ru.pt.product.repository.EntityDefRepository;
import ru.pt.auth.security.SecurityContextHelper;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchemaServiceImpl implements SchemaService {

    private final ContractModelRepository contractModelRepository;
    private final ContractSectionRepository contractSectionRepository;
    private final EntityDefRepository entityDefRepository;
    private final AttributeDefRepository attributeDefRepository;
    private final AuthorizationService authorizationService;
    private final SecurityContextHelper securityContextHelper;

    private AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("User not authenticated"));
    }

    private ContractModelEntity getContractModel(Long tid, String contractCode) {
        return contractModelRepository.findByTidAndCode(tid, contractCode)
                .orElseThrow(() -> new NotFoundException("Contract model not found: " + contractCode));
    }

    private ContractSectionEntity getContractSection(Long tid, Long modelId, String sectionCode) {
        return contractSectionRepository.findByModelIdAndCode(modelId, sectionCode)
                .orElseThrow(() -> new NotFoundException("Section not found: " + sectionCode));
    }

    private EntityDefEntity getEntityDef(Long tid, Long sectionId, String entityCode) {
        return entityDefRepository.findBySectionIdAndCode(sectionId, entityCode)
                .orElseThrow(() -> new NotFoundException("Entity not found: " + entityCode));
    }

    private void validateCode(String code, String fieldName) {
        if (code == null || code.isEmpty()) {
            throw new BadRequestException(fieldName + " code cannot be empty");
        }
        if (!code.matches("^[a-z][a-zA-Z0-9]*$")) {
            throw new BadRequestException(fieldName + " code must contain only latin letters, first char in lowercase");
        }
    }

    @Transactional
    public void newTenantCreated(Long tid) {
        if (tid == null) {
            throw new BadRequestException("Tenant id cannot be null");
        }

        final Long templateTid = 1L;

        // Maps from old to new IDs
        Map<Long, Long> modelIdMap = new HashMap<>();
        Map<Long, Long> sectionIdMap = new HashMap<>();
        Map<Long, Long> entityIdMap = new HashMap<>();

        // 1. Copy contract models (mt_contract_model)
        List<ContractModelEntity> templateModels = contractModelRepository.findByTid(templateTid);
        for (ContractModelEntity templateModel : templateModels) {
            ContractModelEntity newModel = new ContractModelEntity();
            newModel.setTid(tid);
            newModel.setCode(templateModel.getCode());
            newModel.setName(templateModel.getName());

            ContractModelEntity savedModel = contractModelRepository.save(newModel);
            modelIdMap.put(templateModel.getId(), savedModel.getId());
        }

        // 2. Copy sections (mt_contract_section)
        for (Map.Entry<Long, Long> modelEntry : modelIdMap.entrySet()) {
            Long oldModelId = modelEntry.getKey();
            Long newModelId = modelEntry.getValue();

            List<ContractSectionEntity> templateSections = contractSectionRepository.findByModelId(oldModelId);
            for (ContractSectionEntity templateSection : templateSections) {
                ContractSectionEntity newSection = new ContractSectionEntity();
                newSection.setTid(tid);
                newSection.setModelId(newModelId);
                newSection.setCode(templateSection.getCode());
                newSection.setName(templateSection.getName());
                newSection.setPath(templateSection.getPath());

                ContractSectionEntity savedSection = contractSectionRepository.save(newSection);
                sectionIdMap.put(templateSection.getId(), savedSection.getId());
            }
        }

        // 3. Copy entities (mt_entity_def)
        for (Map.Entry<Long, Long> sectionEntry : sectionIdMap.entrySet()) {
            Long oldSectionId = sectionEntry.getKey();
            Long newSectionId = sectionEntry.getValue();

            List<EntityDefEntity> templateEntities = entityDefRepository.findBySectionId(oldSectionId);
            for (EntityDefEntity templateEntity : templateEntities) {
                EntityDefEntity newEntity = new EntityDefEntity();
                newEntity.setTid(tid);
                newEntity.setSectionId(newSectionId);
                newEntity.setCode(templateEntity.getCode());
                newEntity.setName(templateEntity.getName());
                newEntity.setPath(templateEntity.getPath());
                newEntity.setCardinality(templateEntity.getCardinality());

                EntityDefEntity savedEntity = entityDefRepository.save(newEntity);
                entityIdMap.put(templateEntity.getId(), savedEntity.getId());
            }
        }

        // 4. Copy attributes (mt_attribute_def)
        for (Map.Entry<Long, Long> entityEntry : entityIdMap.entrySet()) {
            Long oldEntityId = entityEntry.getKey();
            Long newEntityId = entityEntry.getValue();

            List<AttributeDefEntity> templateAttributes = attributeDefRepository.findByEntityId(oldEntityId);
            for (AttributeDefEntity templateAttr : templateAttributes) {
                AttributeDefEntity newAttr = new AttributeDefEntity();
                newAttr.setTid(tid);
                newAttr.setEntityId(newEntityId);
                newAttr.setCode(templateAttr.getCode());
                newAttr.setName(templateAttr.getName());
                newAttr.setPath(templateAttr.getPath());
                newAttr.setNr(templateAttr.getNr());
                newAttr.setVarCode(templateAttr.getVarCode());
                newAttr.setVarName(templateAttr.getVarName());
                newAttr.setVarPath(templateAttr.getVarPath());
                newAttr.setVarType(templateAttr.getVarType());
                newAttr.setVarValue(templateAttr.getVarValue());
                newAttr.setVarCdm(templateAttr.getVarCdm());
                newAttr.setVarDataType(templateAttr.getVarDataType());

                attributeDefRepository.save(newAttr);
            }
        }
    }

    // ========== SECTIONS ==========

    @Override
    @Transactional
    public List<SectionDto> getSections(Long tid, String contractCode) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.LIST);
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        return contractSectionRepository.findByTidAndModelId(tid, model.getId()).stream()
                .map(s -> new SectionDto(s.getCode(), s.getName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SectionDto createSection(Long tid, String contractCode, SectionDto dto) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.MANAGE);
        
        validateCode(dto.getCode(), "Section");
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        
        if (contractSectionRepository.findByModelIdAndCode(model.getId(), dto.getCode()).isPresent()) {
            throw new BadRequestException("Section with code " + dto.getCode() + " already exists");
        }
        
        ContractSectionEntity entity = new ContractSectionEntity();
        entity.setTid(tid);
        entity.setModelId(model.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setPath(dto.getCode());
        
        ContractSectionEntity saved = contractSectionRepository.save(entity);
        return new SectionDto(saved.getCode(), saved.getName());
    }

    @Override
    @Transactional
    public SectionDto updateSection(Long tid, String contractCode, String code, SectionDto dto) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.MANAGE);
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        ContractSectionEntity entity = getContractSection(tid, model.getId(), code);
        
        // Only name can be updated
        entity.setName(dto.getName());
        ContractSectionEntity saved = contractSectionRepository.save(entity);
        return new SectionDto(saved.getCode(), saved.getName());
    }

    @Override
    @Transactional
    public void deleteSection(Long tid, String contractCode, String code) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.MANAGE);
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        ContractSectionEntity section = getContractSection(tid, model.getId(), code);
        
        // Check if child records exist
        if (!entityDefRepository.findBySectionId(section.getId()).isEmpty()) {
            throw new BadRequestException("Delete child record first");
        }
        
        try {
            contractSectionRepository.delete(section);
        } catch (Exception e) {
            throw new BadRequestException("Delete child record first");
        }
    }

    // ========== ENTITIES ==========

    @Override
    @Transactional
    public List<EntityDefDto> getEntities(Long tid, String contractCode, String sectionCode) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.LIST);
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        ContractSectionEntity section = getContractSection(tid, model.getId(), sectionCode);
        
        return entityDefRepository.findByTidAndSectionId(tid, section.getId()).stream()
                .map(e -> new EntityDefDto(e.getCode(), e.getName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EntityDefDto createEntity(Long tid, String contractCode, String sectionCode, EntityDefDto dto) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.MANAGE);
        
        validateCode(dto.getCode(), "Entity");
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        ContractSectionEntity section = getContractSection(tid, model.getId(), sectionCode);
        
        if (entityDefRepository.findBySectionIdAndCode(section.getId(), dto.getCode()).isPresent()) {
            throw new BadRequestException("Entity with code " + dto.getCode() + " already exists");
        }
        
        EntityDefEntity entity = new EntityDefEntity();
        entity.setTid(tid);
        entity.setSectionId(section.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setPath(sectionCode + "." + dto.getCode());
        entity.setCardinality("SINGLE"); // default
        
        EntityDefEntity saved = entityDefRepository.save(entity);
        return new EntityDefDto(saved.getCode(), saved.getName());
    }

    @Override
    @Transactional
    public EntityDefDto updateEntity(Long tid, String contractCode, String sectionCode, String code, EntityDefDto dto) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.MANAGE);
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        ContractSectionEntity section = getContractSection(tid, model.getId(), sectionCode);
        EntityDefEntity entity = getEntityDef(tid, section.getId(), code);
        
        // Only name can be updated
        entity.setName(dto.getName());
        EntityDefEntity saved = entityDefRepository.save(entity);
        return new EntityDefDto(saved.getCode(), saved.getName());
    }

    @Override
    @Transactional
    public void deleteEntity(Long tid, String contractCode, String sectionCode, String code) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.MANAGE);
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        ContractSectionEntity section = getContractSection(tid, model.getId(), sectionCode);
        EntityDefEntity entity = getEntityDef(tid, section.getId(), code);
        
        // Check if child records exist
        if (!attributeDefRepository.findByEntityId(entity.getId()).isEmpty()) {
            throw new BadRequestException("Delete child record first");
        }
        
        try {
            entityDefRepository.delete(entity);
        } catch (Exception e) {
            throw new BadRequestException("Delete child record first");
        }
    }

    // ========== ATTRIBUTES ==========

    @Override
    @Transactional
    public List<AttributeDefDto> getAttributes(Long tid, String contractCode, String sectionCode, String entityCode) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.LIST);
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        ContractSectionEntity section = getContractSection(tid, model.getId(), sectionCode);
        EntityDefEntity entity = getEntityDef(tid, section.getId(), entityCode);
        
        return attributeDefRepository.findByTidAndEntityId(tid, entity.getId()).stream()
                .map(a -> new AttributeDefDto(
                        a.getCode(),
                        a.getName(),
                        a.getVarDataType(),
                        a.getNr(),
                        a.getVarCode(),
                        a.getVarValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttributeDefDto createAttribute(Long tid, String contractCode, String sectionCode, String entityCode, AttributeDefDto dto) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.MANAGE);
        
        validateCode(dto.getCode(), "Attribute");
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        ContractSectionEntity section = getContractSection(tid, model.getId(), sectionCode);
        EntityDefEntity entity = getEntityDef(tid, section.getId(), entityCode);
        
        if (attributeDefRepository.findByEntityIdAndCode(entity.getId(), dto.getCode()).isPresent()) {
            throw new BadRequestException("Attribute with code " + dto.getCode() + " already exists");
        }
        
        AttributeDefEntity attributeEntity = new AttributeDefEntity();
        attributeEntity.setTid(tid);
        attributeEntity.setEntityId(entity.getId());
        attributeEntity.setCode(dto.getCode());
        attributeEntity.setName(dto.getName());

        String attributePath = sectionCode + "." + entityCode + "." + dto.getCode();
        attributeEntity.setPath(attributePath);

        Long nr = dto.getNr() != null ? dto.getNr() : 1L;
        attributeEntity.setNr(nr);

        String varCode = dto.getVarCode() != null ? dto.getVarCode() : dto.getCode();
        attributeEntity.setVarCode(varCode);
        attributeEntity.setVarName(dto.getName());
        attributeEntity.setVarPath(attributePath);
        attributeEntity.setVarType("IN");
        attributeEntity.setVarValue(dto.getVarValue());
        attributeEntity.setVarCdm(attributePath);
        attributeEntity.setVarDataType(dto.getDataType());

        AttributeDefEntity saved = attributeDefRepository.save(attributeEntity);

        return new AttributeDefDto(
                saved.getCode(),
                saved.getName(),
                saved.getVarDataType(),
                saved.getNr(),
                saved.getVarCode(),
                saved.getVarValue()
        );
    }

    @Override
    @Transactional
    public AttributeDefDto updateAttribute(Long tid, String contractCode, String sectionCode, String entityCode, String code, AttributeDefDto dto) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.MANAGE);
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        ContractSectionEntity section = getContractSection(tid, model.getId(), sectionCode);
        EntityDefEntity entity = getEntityDef(tid, section.getId(), entityCode);
        AttributeDefEntity attributeEntity = attributeDefRepository.findByEntityIdAndCode(entity.getId(), code)
                .orElseThrow(() -> new NotFoundException("Attribute not found: " + code));
        
        // Only name (and derived varName) can be updated
        attributeEntity.setName(dto.getName());
        attributeEntity.setVarName(dto.getName());
        AttributeDefEntity saved = attributeDefRepository.save(attributeEntity);

        return new AttributeDefDto(
                saved.getCode(),
                saved.getName(),
                saved.getVarDataType(),
                saved.getNr(),
                saved.getVarCode(),
                saved.getVarValue()
        );
    }

    @Override
    @Transactional
    public void deleteAttribute(Long tid, String contractCode, String sectionCode, String entityCode, String code) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CONTRACT, null, null, AuthZ.Action.MANAGE);
        
        ContractModelEntity model = getContractModel(tid, contractCode);
        ContractSectionEntity section = getContractSection(tid, model.getId(), sectionCode);
        EntityDefEntity entity = getEntityDef(tid, section.getId(), entityCode);
        AttributeDefEntity attributeEntity = attributeDefRepository.findByEntityIdAndCode(entity.getId(), code)
                .orElseThrow(() -> new NotFoundException("Attribute not found: " + code));
        
        try {
            attributeDefRepository.delete(attributeEntity);
        } catch (Exception e) {
            throw new BadRequestException("Delete child record first");
        }
    }

}
