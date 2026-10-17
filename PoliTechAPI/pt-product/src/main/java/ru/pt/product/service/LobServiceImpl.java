package ru.pt.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.api.dto.product.LobCover;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.VarDataType;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;
import ru.pt.api.service.schema.SchemaService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.LobEntity;
import ru.pt.product.entity.ProductEntity;
import ru.pt.product.repository.LobRepository;
import ru.pt.product.repository.ProductRepository;
import ru.pt.api.dto.product.LobVar;
import ru.pt.api.dto.schema.AttributeDefaultState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import ru.pt.api.dto.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import ru.pt.api.dto.refs.RecordStatus;

@Component
@RequiredArgsConstructor
public class LobServiceImpl implements LobService {

    private final Logger log = LoggerFactory.getLogger(LobServiceImpl.class);

    private final LobRepository lobRepository;
    private final ProductRepository productRepository;
    /** Без цикла с {@link ProductServiceImpl} (он зависит от LobService). */
    private final ObjectProvider<ProductService> productService;
    private final ObjectMapper objectMapper;
    private final SecurityContextHelper securityContextHelper;
    private final AuthorizationService authService;
    private final SchemaService schemaService;
    /**
     * Get current authenticated user from security context
     * @return AuthenticatedUser representing the current user
     * @throws ru.pt.api.dto.exception.UnauthorizedException if user is not authenticated
     */
    protected AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> new UnauthorizedException("Unable to get current user from context"));
    }

 

    @Override
    public List<LobModel> listActiveSummaries(Long tenantId) {
        authService.check(getCurrentUser(), AuthZ.ResourceType.LOB, null, tenantId, AuthZ.Action.LIST);

        return lobRepository.listActiveSummaries(tenantId).stream()
                .map(row -> {
                    LobModel m = new LobModel();
                    m.setId(((Number) row[0]).longValue());
                    m.setMpCode((String) row[1]);
                    m.setMpName((String) row[2]);
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Override
    public LobModel getByCode(Long tenantId, String code) {

        LobEntity lob = lobRepository.findByCode(tenantId, code).orElse(null);
        if (lob == null) {
            return null;
        }
        return getById(tenantId, lob.getId());
    }

    @Override
    public LobModel getById(Long tenantId, Long id) {
        authService.check(getCurrentUser(), AuthZ.ResourceType.LOB, String.valueOf(id), tenantId, AuthZ.Action.VIEW);

        LobEntity lob = lobRepository.findById(tenantId, id).orElse(null);
        if (lob == null) {
            return null;
        }
        try {
            return objectMapper.readValue(lob.getLob(), LobModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public LobModel create(Long tenantId, LobModel payload) {
        authService.check(getCurrentUser(), AuthZ.ResourceType.LOB, null, tenantId, AuthZ.Action.MANAGE);

        String mpCode = payload.getMpCode();
        if (getByCode(tenantId, mpCode) != null) {
            // проверка на дуюль кода
            throw new BadRequestException("Продукт с кодом " + mpCode + " уже существует");
        }

        String mpName = payload.getMpName();
        if (mpCode == null || mpCode.trim().isEmpty()) {
            throw new BadRequestException("mpCode must not be empty");
        }
        if (mpName == null || mpName.trim().isEmpty()) {
            throw new BadRequestException("mpName must not be empty");
        }

        if (payload.getMpVars() != null) {
            Set<String> varCodes = new HashSet<>();
            for (var var : payload.getMpVars()) {
                if (!varCodes.add(var.getVarCode())) {
                    throw new BadRequestException("Duplicate varCode found: " + var.getVarCode());
                }
            }
        }

        if (payload.getMpCovers() != null) {
            Set<String> coverCodes = new HashSet<>();
            for (var cover : payload.getMpCovers()) {
                if (!coverCodes.add(cover.getCoverCode())) {
                    throw new BadRequestException("Duplicate coverCode found: " + cover.getCoverCode());
                }
            }
        }

        long nextId = lobRepository.nextLobId();
        payload.setId(nextId);

        //payload = syncCoversVars(payload);
        // проблема при импорте продукта с другой БД не совпадают id и иззза этого не работают некоторые фичи.
        // поэтому при создании лоба берем модель документа из справочника и по varCode удаляем ненужное
        List<LobVar> schemaLob = schemaService.getAttributes( tenantId, SchemaService.INSURANCE_CONTRACT);
        if (schemaLob == null) {
            throw new UnprocessableEntityException("Schema LOB not found", "SCHEMA", "NOT_FOUND", "schemaLob");
        }

        Map<String, LobVar> schemaVarMap = new HashMap<>();
        for (LobVar schemaVar : schemaLob) {
            schemaVarMap.put(schemaVar.getVarCode(), schemaVar);
        }
        for (LobVar payloadVar : payload.getMpVars()) {
            LobVar schemaVar = schemaVarMap.get(payloadVar.getVarCode());
            if (schemaVar != null) {
                payloadVar.setId(schemaVar.getId());
                payloadVar.setParent_id(schemaVar.getParent_id());
                payloadVar.setIsSystem(schemaVar.getIsSystem());
                payloadVar.setName(schemaVar.getName());
                payloadVar.setDefaultState(schemaVar.getDefaultState());
                // DISABLED в схеме → в LOB isDeleted
                if (AttributeDefaultState.isDisabled(schemaVar.getDefaultState())) {
                    payloadVar.setIsDeleted(true);
                }
            } else {
                payloadVar.setIsDeleted(true);
            }
        }


        Set<String> payloadVarCodes = new HashSet<>();
        for (LobVar payloadVar : payload.getMpVars()) {
            if (!payloadVar.getIsDeleted()) {
                payloadVarCodes.add(payloadVar.getVarCode());
            }
        }

        Set<String> schemaVarCodes = new HashSet<>();
        for (LobVar schemaVar : schemaLob) {
            schemaVarCodes.add(schemaVar.getVarCode());
        }

        for (LobVar schemaVar : schemaLob) {
            if (!payloadVarCodes.contains(schemaVar.getVarCode())
                    || AttributeDefaultState.isDisabled(schemaVar.getDefaultState())) {
                schemaVar.setIsDeleted(true);
            }
        }

        // добавить в новый массив текстовые переменные из старого
        for ( LobVar mpVar : payload.getMpVars()) {
            if ( "TEXT".equals(mpVar.getVarType()) && !schemaVarCodes.contains(mpVar.getVarCode()) ) {
                schemaLob.add(mpVar);
            }
        }

        payload.setMpVars(schemaLob);

        LobEntity lob = new LobEntity();
        lob.setId(nextId);
        lob.setTid(tenantId);
        lob.setCode(payload.getMpCode());
        lob.setName(payload.getMpName());
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        lob.setLob(payloadJson);

        try {
            return objectMapper.readValue(lobRepository.save(lob).getLob(), LobModel.class);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("LOB with code already exists: ");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public boolean softDeleteById(Long tenantId, Long id) {
        authService.check(getCurrentUser(), AuthZ.ResourceType.LOB, String.valueOf(id), tenantId, AuthZ.Action.MANAGE);

        LobEntity lob = lobRepository.findById(tenantId, id).orElseThrow(() -> new NotFoundException("Lob not found"));
        lob.setRecordStatus(RecordStatus.SUSPENDED.name());
        lobRepository.save(lob);
        return true;
    }

    // create method update by id. get lob from repository by id. if lob is not found, throw not found exception.
    // check that code is not changed. if changed, throw bad request exception.
    // update lob with payload. return updated lob.
    @Transactional
    @Override
    public LobModel update(Long tenantId, LobModel lobModel) {
        authService.check(getCurrentUser(), AuthZ.ResourceType.LOB, lobModel.getMpCode(), tenantId, AuthZ.Action.MANAGE);

        LobEntity lob = lobRepository.findByCode(tenantId, lobModel.getMpCode()).orElseThrow(() -> new NotFoundException("Lob not found"));

        //lobModel = syncCoversVars(lobModel);

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(lobModel);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        lob.setLob(payloadJson);
        lob.setName(lobModel.getMpName());
        try {
            return objectMapper.readValue(lobRepository.save(lob).getLob(), LobModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    
    private LobModel syncCoversVars(LobModel lobModel) {
        if (lobModel == null) {
            return null;
        }

        List<LobVar> existingVars = lobModel.getMpVars();
        List<LobVar> vars;

        if (existingVars == null) {
            vars = new ArrayList<>();
        } else {
            // Create a new list to avoid potential issues with unmodifiable lists
            vars = new ArrayList<>(existingVars);
        }

        // get id of covers var by code. if not found or found more than 1 time that raise exception - unprocessable entity exception
        List<LobVar> coversNodes = vars.stream()
                .filter(v -> v != null && "covers".equals(v.getVarCode()))
                .toList();
        if (coversNodes.isEmpty()) {
            throw new UnprocessableEntityException("LOB mpVars: no variable with varCode 'covers'");
        }
        if (coversNodes.size() > 1) {
            throw new UnprocessableEntityException("LOB mpVars: multiple variables with varCode 'covers'");
        }
        LobVar coversVar = coversNodes.get(0);
        Long coversId = coversVar.getId();
        if (coversId == null) {
            throw new UnprocessableEntityException("LOB mpVars: covers variable has no id");
        }

        // Remove existing cover-related vars recursively by tree structure
        Set<Long> subtreeIds = coversSubtreeIds(vars, coversVar.getId());
        removeVarsInCoversSubtree(vars, subtreeIds);

        List<LobCover> mpCovers = lobModel.getMpCovers();

        if (mpCovers != null) {
            for (LobCover cover : mpCovers) {
                if (cover == null || cover.getCoverCode() == null || cover.getCoverCode().isBlank()) {
                    continue;
                }
                String code = cover.getCoverCode().trim();
                LobVar coverVar = coverVar(code);
                coverVar.setParent_id(coversId);
                coverVar.setId(schemaService.nextId());
                coverVar.setIsSystem(true);
                vars.add(coverVar);

                addPvVarAsChild(vars, PvVar.varSumInsured(code), coverVar.getId());
                addPvVarAsChild(vars, PvVar.varPremium(code), coverVar.getId());
                addPvVarAsChild(vars, PvVar.varDeductibleNr(code), coverVar.getId());
                addPvVarAsChild(vars, PvVar.varLimitMin(code), coverVar.getId());
                addPvVarAsChild(vars, PvVar.varLimitMax(code), coverVar.getId());
            }
        }

        lobModel.setMpVars(vars);
        return lobModel;
    }

    /**
     * All {@code id} values in the subtree rooted at {@code rootId} ({@code parent_id} links), including {@code rootId}.
     */
    private static Set<Long> coversSubtreeIds(List<LobVar> vars, Long rootId) {
        Set<Long> ids = new HashSet<>();
        ids.add(rootId);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (LobVar v : vars) {
                if (v == null || v.getId() == null) {
                    continue;
                }
                if (v.getParent_id() != null && ids.contains(v.getParent_id()) && ids.add(v.getId())) {
                    changed = true;
                }
            }
        }
        ids.remove(rootId);
        return ids;
    }

    private static void removeVarsInCoversSubtree(List<LobVar> vars, Set<Long> subtreeIds) {
        vars.removeIf(v -> {
            if (v == null) {
                return false;
            }
            if (v.getId() != null && subtreeIds.contains(v.getId())) {
                return true;
            }
            return v.getParent_id() != null && subtreeIds.contains(v.getParent_id());
        });
    }

    private void addPvVarAsChild(List<LobVar> vars, PvVar p, Long parentId) {
        LobVar l = lobVarFromPvVar(p);
        l.setParent_id(parentId);
        l.setId(schemaService.nextId());
        l.setIsSystem(true);
        vars.add(l);
    }

    private static LobVar lobVarFromPvVar(PvVar p) {
        LobVar l = new LobVar();
        l.setVarCode(p.getVarCode());
        l.setVarName(p.getVarName());
        l.setVarPath(p.getVarPath());
        l.setVarType(p.getVarType());
        l.setVarValue(p.getVarValue() != null ? p.getVarValue() : "");
        l.setVarDataType(p.getVarDataType());
        l.setVarNr(p.getVarNr());
        l.setVarList(p.getVarList());
        l.setIsSystem(p.getIsSystem());
        l.setIsDeleted(p.getIsDeleted());
        l.setName(p.getName());
        return l;
    }

    private static LobVar coverVar(String code) {
        LobVar l = new LobVar();
        l.setVarCode("co_" + code);
        l.setVarName("Покрытие " + code);
        l.setVarPath("insuredObjects[0].covers[?(@.cover.code == \"" + code + "\")]");
        l.setVarType("OBJECT");
        l.setVarDataType(VarDataType.OBJECT);
        l.setVarValue("");
        return l;
    }

    @Override
    @Transactional
    public void appendSchemaAttributeAsDeleted(Long tenantId, LobVar schemaVar) {
        if (schemaVar == null || schemaVar.getVarCode() == null || schemaVar.getVarCode().isBlank()) {
            return;
        }
        String code = schemaVar.getVarCode().trim();

        for (LobEntity entity : lobRepository.findActiveByTid(tenantId)) {
            LobModel model;
            try {
                model = objectMapper.readValue(entity.getLob(), LobModel.class);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException("Failed to parse LOB JSON for code=" + entity.getCode(), ex);
            }

            List<LobVar> vars = model.getMpVars();
            if (vars == null) {
                vars = new ArrayList<>();
            } else {
                vars = new ArrayList<>(vars);
            }
            boolean exists = vars.stream()
                    .anyMatch(v -> v != null && code.equals(v.getVarCode()));
            if (!exists) {
                vars.add(copyAsDeletedLobVar(schemaVar));
                model.setMpVars(vars);
                try {
                    entity.setLob(objectMapper.writeValueAsString(model));
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException("Failed to write LOB JSON for code=" + entity.getCode(), ex);
                }
                lobRepository.save(entity);
            }

            appendAttributeToDevProductsOfLob(tenantId, entity.getCode(), schemaVar, code);
        }
    }

    /**
     * DEV-версии продуктов LOB через {@link ProductService#addVar}
     * ({@code pt_products} ACTIVE с {@code lob} и {@code dev_version_no}).
     */
    private void appendAttributeToDevProductsOfLob(
            Long tenantId, String lobCode, LobVar schemaVar, String varCode) {
        if (lobCode == null || lobCode.isBlank()) {
            return;
        }
        ProductService productsApi = productService.getObject();
        List<ProductEntity> products =
                productRepository.findActiveByTidAndLobWithDevVersion(tenantId, lobCode);
        for (ProductEntity product : products) {
            Long versionNo = product.getDevVersionNo();
            if (versionNo == null) {
                continue;
            }
            try {
                productsApi.addVar(
                        tenantId, product.getId(), versionNo, copyAsDeletedPvVar(schemaVar));
            } catch (BadRequestException ex) {
                // varCode уже есть в продукте
                log.debug(
                        "Skip addVar (already exists): productId={}, versionNo={}, varCode={}",
                        product.getId(),
                        versionNo,
                        varCode);
            } catch (UnprocessableEntityException | NotFoundException ex) {
                log.warn(
                        "Skip addVar for productId={}, versionNo={}, lob={}: {}",
                        product.getId(),
                        versionNo,
                        lobCode,
                        ex.getMessage());
            }
        }
    }

    private static LobVar copyAsDeletedLobVar(LobVar src) {
        return LobVar.builder()
                .id(src.getId())
                .parent_id(src.getParent_id())
                .varCode(src.getVarCode())
                .varName(src.getVarName())
                .varPath(src.getVarPath())
                .varType(src.getVarType())
                .varDataType(src.getVarDataType())
                .varValue(src.getVarValue() != null ? src.getVarValue() : "")
                .varNr(src.getVarNr())
                .varList(src.getVarList())
                .isSystem(src.getIsSystem())
                .isDeleted(true)
                .name(src.getName() != null ? src.getName() : src.getVarName())
                .build();
    }

    private static PvVar copyAsDeletedPvVar(LobVar src) {
        PvVar v = new PvVar(src);
        v.setIsDeleted(true);
        v.setIsSystem(src.getIsSystem());
        v.setIsOptional(true);
        v.setIsTarifFactor(false);
        return v;
    }
}