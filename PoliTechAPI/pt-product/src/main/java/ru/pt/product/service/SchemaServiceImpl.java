package ru.pt.product.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.LobVar;
import ru.pt.api.dto.product.VarDataType;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.schema.SchemaService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.TenantService;
import ru.pt.product.entity.AttributeDefEntity;
import ru.pt.product.entity.ContractModelEntity;
import ru.pt.product.repository.AttributeDefRepository;
import ru.pt.product.repository.ContractModelRepository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchemaServiceImpl implements SchemaService {

    /**
     * Как в SQL: {@code ORDER BY parent_id NULLS FIRST, var_ord NULLS LAST, id}.
     */
    private static final Comparator<AttributeDefEntity> SCHEMA_TREE_ORDER = Comparator
            .comparing((AttributeDefEntity e) -> e.getParentId() != null)
            .thenComparing(AttributeDefEntity::getParentId, Comparator.nullsFirst(Long::compareTo))
            .thenComparing(e -> e.getVarOrd() != null ? e.getVarOrd() : Long.MAX_VALUE)
            .thenComparing(AttributeDefEntity::getId);

    private final ContractModelRepository contractModelRepository;
    private final AttributeDefRepository attributeDefRepository;
    private final AuthorizationService authorizationService;
    private final SecurityContextHelper securityContextHelper;
    private final TenantService tenantService;

    @PersistenceContext
    private EntityManager entityManager;

    private AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("User not authenticated"));
    }

    private Long tenantIdFromCode(String tenantCode) {
        return tenantService.getTenant(tenantCode).id();
    }

    private void assertNodeInSchema(Long nodeId, Long tid, String documentId) {
        if (!attributeDefRepository.existsByIdAndTenantIdAndDocumentId(nodeId, tid, documentId)) {
            throw new NotFoundException("Schema node not found: " + nodeId);
        }
    }

    private static long parseVarOrd(String varNr) {
        if (varNr == null || varNr.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(varNr.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static String varDataTypeToDb(VarDataType t) {
        return t == null ? VarDataType.STRING.name() : t.name();
    }

    private void validateCode(String code, String fieldName) {
        if (code == null || code.isEmpty()) {
            throw new BadRequestException(fieldName + " code cannot be empty");
        }
        if (!code.matches("^[a-z][a-zA-Z0-9]*$")) {
            throw new BadRequestException(fieldName + " code must contain only latin letters, first char in lowercase");
        }
    }

    private static LobVar toLobVar(AttributeDefEntity e) {
        VarDataType vdt = null;
        if (e.getVarDataType() != null && !e.getVarDataType().isBlank()) {
            try {
                vdt = VarDataType.valueOf(e.getVarDataType().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                vdt = VarDataType.STRING;
            }
        }
        return LobVar.builder()
                .id(e.getId())
                .parent_id(e.getParentId())
                .varNr(e.getVarOrd() != null ? e.getVarOrd().toString() : null)
                .varCode(e.getVarCode())
                .varName(e.getVarName())
                .varType(e.getVarType())
                .varDataType(vdt)
                .varList(e.getVarList())
                .isSystem(e.isSystem())
                .varPath(e.getVarPath())
                .varCdm(e.getVarCdm())
                .varValue(e.getVarValue() != null ? e.getVarValue() : "")
                .isDeleted(false)
                .build();
    }

    private static void applyLobVarToEntity(LobVar v, AttributeDefEntity e) {
        e.setParentId(v.getParent_id());
        e.setVarCode(v.getVarCode());
        e.setVarName(v.getVarName());
        e.setVarPath(v.getVarPath());
        e.setVarOrd(parseVarOrd(v.getVarNr()));
        e.setVarType(v.getVarType());
        e.setVarDataType(varDataTypeToDb(v.getVarDataType()));
        e.setVarValue(v.getVarValue());
        e.setVarList(v.getVarList());
        e.setVarCdm(v.getVarCdm());
        e.setSystem(v.isSystem());
        String displayName = v.getVarName() == null ? null
                : (v.getVarName().length() > 250 ? v.getVarName().substring(0, 250) : v.getVarName());
        e.setCode(v.getVarCode());
        e.setName(displayName);
    }

    @Override
    @Transactional
    public void newTenantCreated(Long tid) {
        if (tid == null) {
            throw new BadRequestException("Tenant id cannot be null");
        }

        final Long templateTid = 1L;

        List<ContractModelEntity> templateModels = contractModelRepository.findByTid(templateTid);
        for (ContractModelEntity templateModel : templateModels) {
            ContractModelEntity newModel = new ContractModelEntity();
            newModel.setTid(tid);
            newModel.setCode(templateModel.getCode());
            newModel.setName(templateModel.getName());
            contractModelRepository.save(newModel);
        }

        List<AttributeDefEntity> templateAttrs = attributeDefRepository.findByTenantId(templateTid);
        if (templateAttrs.isEmpty()) {
            return;
        }

        Map<Long, List<AttributeDefEntity>> childrenByParent = new HashMap<>();
        List<AttributeDefEntity> roots = new ArrayList<>();
        for (AttributeDefEntity a : templateAttrs) {
            if (a.getParentId() == null) {
                roots.add(a);
            } else {
                childrenByParent.computeIfAbsent(a.getParentId(), k -> new ArrayList<>()).add(a);
            }
        }

        ArrayDeque<AttributeDefEntity> queue = new ArrayDeque<>(roots);
        List<AttributeDefEntity> bfsOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            AttributeDefEntity node = queue.removeFirst();
            bfsOrder.add(node);
            List<AttributeDefEntity> ch = childrenByParent.get(node.getId());
            if (ch != null) {
                queue.addAll(ch);
            }
        }
        Set<Long> seenIds = new HashSet<>();
        for (AttributeDefEntity x : bfsOrder) {
            seenIds.add(x.getId());
        }
        for (AttributeDefEntity a : templateAttrs) {
            if (seenIds.add(a.getId())) {
                bfsOrder.add(a);
            }
        }

        Map<Long, Long> oldToNewId = new HashMap<>();
        for (AttributeDefEntity t : bfsOrder) {
            Long newParentId = t.getParentId() == null ? null : oldToNewId.get(t.getParentId());
            AttributeDefEntity n = copyAttributeForTenant(t, tid, newParentId);
            AttributeDefEntity saved = attributeDefRepository.save(n);
            oldToNewId.put(t.getId(), saved.getId());
        }
    }

    private static AttributeDefEntity copyAttributeForTenant(AttributeDefEntity t, Long newTid, Long newParentId) {
        AttributeDefEntity n = new AttributeDefEntity();
        n.setTenantId(newTid);
        n.setDocumentId(t.getDocumentId());
        n.setParentId(newParentId);
        n.setVarCode(t.getVarCode());
        n.setVarName(t.getVarName());
        n.setVarPath(t.getVarPath());
        n.setVarOrd(t.getVarOrd() != null ? t.getVarOrd() : 0L);
        n.setVarType(t.getVarType());
        n.setVarCardinality(t.getVarCardinality() != null ? t.getVarCardinality() : "SINGLE");
        n.setVarDataType(t.getVarDataType());
        n.setVarValue(t.getVarValue());
        n.setVarCdm(t.getVarCdm());
        n.setVarList(t.getVarList());
        n.setCode(t.getCode());
        n.setName(t.getName());
        n.setSystem(t.isSystem());
        return n;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LobVar> getAttributes(String tenantCode, String contractCode) {
        AuthenticatedUser user = getCurrentUser();
        Long tid = tenantIdFromCode(tenantCode);
        authorizationService.check(user, AuthZ.ResourceType.TENANT, null, tid, AuthZ.Action.LIST);

        List<AttributeDefEntity> rows =
                attributeDefRepository.findByTenantIdAndDocumentId(tid, contractCode);
        rows.sort(SCHEMA_TREE_ORDER);
        return rows.stream()
                .map(SchemaServiceImpl::toLobVar)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addAttribute(String tenantCode, String contractCode, LobVar lobVar) {
        AuthenticatedUser user = getCurrentUser();
        Long tid = tenantIdFromCode(tenantCode);
        String documentId = contractCode;
        authorizationService.check(user, AuthZ.ResourceType.TENANT, null, tid, AuthZ.Action.MANAGE);

        if (lobVar == null) {
            throw new BadRequestException("Body is required");
        }
        validateCode(lobVar.getVarCode(), "Attribute");
        if (lobVar.getVarName() == null || lobVar.getVarName().isBlank()) {
            throw new BadRequestException("varName cannot be empty");
        }
        if (lobVar.getVarPath() == null || lobVar.getVarPath().isBlank()) {
            throw new BadRequestException("varPath cannot be empty");
        }
        if (lobVar.getVarType() == null || lobVar.getVarType().isBlank()) {
            throw new BadRequestException("varType cannot be empty");
        }

        if (lobVar.getParent_id() != null) {
            assertNodeInSchema(lobVar.getParent_id(), tid, documentId);
        }

        AttributeDefEntity e = new AttributeDefEntity();
        e.setTenantId(tid);
        e.setDocumentId(documentId);
        e.setVarCardinality("SINGLE");
        applyLobVarToEntity(lobVar, e);
        AttributeDefEntity saved = attributeDefRepository.save(e);
        lobVar.setId(saved.getId());
    }

    @Override
    @Transactional
    public void updateAttribute(String tenantCode, String contractCode, LobVar lobVar) {
        AuthenticatedUser user = getCurrentUser();
        Long tid = tenantIdFromCode(tenantCode);
        String documentId = contractCode;
        authorizationService.check(user, AuthZ.ResourceType.TENANT, null, tid, AuthZ.Action.MANAGE);

        if (lobVar == null || lobVar.getId() == null) {
            throw new BadRequestException("id is required");
        }
        assertNodeInSchema(lobVar.getId(), tid, documentId);

        if (lobVar.getParent_id() != null) {
            if (lobVar.getParent_id().equals(lobVar.getId())) {
                throw new BadRequestException("parent_id cannot equal id");
            }
            assertNodeInSchema(lobVar.getParent_id(), tid, documentId);
        }

        validateCode(lobVar.getVarCode(), "Attribute");
        if (lobVar.getVarName() == null || lobVar.getVarName().isBlank()) {
            throw new BadRequestException("varName cannot be empty");
        }
        if (lobVar.getVarPath() == null || lobVar.getVarPath().isBlank()) {
            throw new BadRequestException("varPath cannot be empty");
        }
        if (lobVar.getVarType() == null || lobVar.getVarType().isBlank()) {
            throw new BadRequestException("varType cannot be empty");
        }

        AttributeDefEntity e = attributeDefRepository.findById(lobVar.getId())
                .orElseThrow(() -> new NotFoundException("Schema node not found: " + lobVar.getId()));
        if (!tid.equals(e.getTenantId()) || !documentId.equals(e.getDocumentId())) {
            throw new NotFoundException("Schema node not found: " + lobVar.getId());
        }
        applyLobVarToEntity(lobVar, e);
        attributeDefRepository.save(e);
    }

    @Override
    @Transactional
    public void deleteAttribute(String tenantCode, String contractCode, LobVar lobVar) {
        AuthenticatedUser user = getCurrentUser();
        Long tid = tenantIdFromCode(tenantCode);
        String documentId = contractCode;
        authorizationService.check(user, AuthZ.ResourceType.TENANT, null, tid, AuthZ.Action.MANAGE);

        if (lobVar == null || lobVar.getId() == null) {
            throw new BadRequestException("id is required");
        }
        assertNodeInSchema(lobVar.getId(), tid, documentId);

        int n = entityManager.createNativeQuery("""
                WITH RECURSIVE sub AS (
                    SELECT id FROM mt_attribute_def
                    WHERE id = :rootId AND tenant_id = :tid AND document_id = :docId
                    UNION ALL
                    SELECT c.id FROM mt_attribute_def c
                    INNER JOIN sub p ON c.parent_id = p.id
                    WHERE c.tenant_id = :tid AND c.document_id = :docId
                )
                DELETE FROM mt_attribute_def WHERE id IN (SELECT id FROM sub)
                """)
                .setParameter("rootId", lobVar.getId())
                .setParameter("tid", tid)
                .setParameter("docId", documentId)
                .executeUpdate();
        if (n == 0) {
            throw new NotFoundException("Schema node not found: " + lobVar.getId());
        }
    }
}
