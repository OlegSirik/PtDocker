package ru.pt.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
   
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("User not authenticated"));
    }

//    private Long tenantIdFromCode(String tenantCode) {
//        return tenantService.getTenant(tenantCode).id();
//    }

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

    /** Имя поля в JSON: {@code code}, иначе {@code varCode}. */
    private static String attributeJsonKey(AttributeDefEntity e) {
        if (e.getCode() != null && !e.getCode().isBlank()) {
            return e.getCode().trim();
        }
        return e.getVarCode() != null ? e.getVarCode() : "";
    }

    /**
     * Лист — пустая строка; иначе объект с ключами-потомками ({@code parent_id} = id узла).
     * Контейнер массива ({@code ARRAY} / {@code MULT}) — один шаблон элемента: {@code [{ ... }]}.
     */
    private JsonNode buildAttributeJsonSubtree(
            AttributeDefEntity e,
            Map<Long, List<AttributeDefEntity>> childrenByParent) {
        List<AttributeDefEntity> raw = childrenByParent.getOrDefault(e.getId(), List.of());
        if (raw.isEmpty()) {
            return objectMapper.getNodeFactory().textNode("");
        }
        List<AttributeDefEntity> kids = new ArrayList<>(raw);
        kids.sort(SCHEMA_TREE_ORDER);
        ObjectNode childObject = buildChildObjectNode(kids, childrenByParent);
        if (isSchemaArrayContainer(e)) {
            ArrayNode arr = objectMapper.createArrayNode();
            if (!childObject.isEmpty()) {
                arr.add(childObject);
            }
            return arr;
        }
        return childObject;
    }

    private ObjectNode buildChildObjectNode(
            List<AttributeDefEntity> kids,
            Map<Long, List<AttributeDefEntity>> childrenByParent) {
        ObjectNode obj = objectMapper.createObjectNode();
        for (AttributeDefEntity c : kids) {
            String key = attributeJsonKey(c);
            if (!key.isEmpty()) {
                obj.set(key, buildAttributeJsonSubtree(c, childrenByParent));
            }
        }
        return obj;
    }

    private static String normalizedVarType(String varType) {
        if (varType == null || varType.isBlank()) {
            return "";
        }
        return varType.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizedVarCardinality(String cardinality) {
        if (cardinality == null || cardinality.isBlank()) {
            return "";
        }
        return cardinality.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Контейнер «массив в JSON»: {@code var_type = ARRAY} или {@code var_cardinality = MULT}.
     * Сериализуется как {@code [{ ... }]} — массив из одного объекта-шаблона по детям.
     */
    private static boolean isSchemaArrayContainer(AttributeDefEntity e) {
        if ("ARRAY".equals(normalizedVarType(e.getVarType()))) {
            return true;
        }
        return "MULT".equals(normalizedVarCardinality(e.getVarCardinality()));
    }

    private static boolean varCodeInValues(AttributeDefEntity e, Map<String, String> varValues) {
        String vc = e.getVarCode();
        return vc != null && varValues.containsKey(vc);
    }

    private static boolean keepByFilter(AttributeDefEntity e, Map<String, String> varValues) {
        return e.isSystem() || varCodeInValues(e, varValues);
    }

    /**
     * Лист — строка из {@code varValues} по {@code var_code} или {@code var_value} схемы;
     * иначе объект/массив из отфильтрованных потомков. Узел не попадает в JSON, если он не
     * {@link #keepByFilter} и не имеет непустых потомков; пустые контейнеры OBJECT/ARRAY
     * (в т.ч. при {@code var_cardinality = MULT}) отбрасываются, кроме случая когда сам узел
     * {@link #keepByFilter} — тогда остаётся {@code {}} или {@code [{}]}.
     */
    private Optional<JsonNode> buildFilteredAttributeSubtree(
            AttributeDefEntity e,
            Map<Long, List<AttributeDefEntity>> childrenByParent,
            Map<String, String> varValues) {
        List<AttributeDefEntity> raw = childrenByParent.getOrDefault(e.getId(), List.of());
        boolean hasKids = !raw.isEmpty();

        if (!hasKids) {
            if (!keepByFilter(e, varValues)) {
                return Optional.empty();
            }
            String vc = e.getVarCode();
            String text;
            if (vc != null && varValues.containsKey(vc)) {
                String v = varValues.get(vc);
                text = v != null ? v : "";
            } else {
                text = e.getVarValue() != null ? e.getVarValue() : "";
            }
            return Optional.of(objectMapper.getNodeFactory().textNode(text));
        }

        List<AttributeDefEntity> kids = new ArrayList<>(raw);
        kids.sort(SCHEMA_TREE_ORDER);

        ObjectNode childObject = objectMapper.createObjectNode();
        for (AttributeDefEntity c : kids) {
            String key = attributeJsonKey(c);
            if (!key.isEmpty()) {
                buildFilteredAttributeSubtree(c, childrenByParent, varValues)
                        .ifPresent(node -> childObject.set(key, node));
            }
        }
        if (childObject.isEmpty()) {
            if (!keepByFilter(e, varValues)) {
                return Optional.empty();
            }
            if (isSchemaArrayContainer(e)) {
                ArrayNode arr = objectMapper.createArrayNode();
                arr.add(objectMapper.createObjectNode());
                return Optional.of(arr);
            }
            return Optional.of(objectMapper.createObjectNode());
        }
        if (isSchemaArrayContainer(e)) {
            ArrayNode arr = objectMapper.createArrayNode();
            arr.add(childObject);
            return Optional.of(arr);
        }
        return Optional.of(childObject);
    }

    /**
     * Узлы с {@code parent_id == null} — технические корни; в JSON не выводятся, на верхний уровень
     * попадают только их прямые потомки.
     */
    private void appendMetadataRootsChildrenToDocument(
            ObjectNode document,
            List<AttributeDefEntity> roots,
            Map<Long, List<AttributeDefEntity>> childrenByParent) {
        for (AttributeDefEntity r : roots) {
            List<AttributeDefEntity> raw = childrenByParent.getOrDefault(r.getId(), List.of());
            if (raw.isEmpty()) {
                continue;
            }
            List<AttributeDefEntity> kids = new ArrayList<>(raw);
            kids.sort(SCHEMA_TREE_ORDER);
            for (AttributeDefEntity c : kids) {
                String key = attributeJsonKey(c);
                if (!key.isEmpty()) {
                    document.set(key, buildAttributeJsonSubtree(c, childrenByParent));
                }
            }
        }
    }

    private void appendFilteredMetadataRootsChildrenToDocument(
            ObjectNode document,
            List<AttributeDefEntity> roots,
            Map<Long, List<AttributeDefEntity>> childrenByParent,
            Map<String, String> varValues) {
        for (AttributeDefEntity r : roots) {
            List<AttributeDefEntity> raw = childrenByParent.getOrDefault(r.getId(), List.of());
            if (raw.isEmpty()) {
                continue;
            }
            List<AttributeDefEntity> kids = new ArrayList<>(raw);
            kids.sort(SCHEMA_TREE_ORDER);
            for (AttributeDefEntity c : kids) {
                String key = attributeJsonKey(c);
                if (!key.isEmpty()) {
                    buildFilteredAttributeSubtree(c, childrenByParent, varValues)
                            .ifPresent(node -> document.set(key, node));
                }
            }
        }
    }

    private void validateCode(String code, String fieldName) {
        if (code == null || code.isEmpty()) {
            throw new BadRequestException(fieldName + " code cannot be empty");
        }
        if (!code.matches("^[a-z][a-zA-Z0-9_-]*$")) {
            throw new BadRequestException(fieldName
                    + " code: first char lowercase latin, then latin letters, digits, '_' or '-'");
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
                .name(e.getName())
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
        e.setSystem(v.getIsSystem());
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
    public List<LobVar> getAttributes(Long tenantId, String contractCode) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.TENANT, null, tenantId, AuthZ.Action.LIST);

        List<AttributeDefEntity> rows =
                attributeDefRepository.findByTenantIdAndDocumentId(tenantId, contractCode);
        rows.sort(SCHEMA_TREE_ORDER);
        return rows.stream()
                .map(SchemaServiceImpl::toLobVar)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String getAttributesMetadataJson(Long tenantId, String contractCode) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.TENANT, null, tenantId, AuthZ.Action.LIST);

        List<AttributeDefEntity> rows =
                attributeDefRepository.findByTenantIdAndDocumentId(tenantId, contractCode);

        // delete rows where isDeleted is true and var_type != 'IN','OBJECT'       
        rows.removeIf(row -> !row.getVarType().equals("IN") && !row.getVarType().equals("OBJECT"));

        Map<Long, List<AttributeDefEntity>> childrenByParent = new HashMap<>();
        List<AttributeDefEntity> roots = new ArrayList<>();

        for (AttributeDefEntity row : rows) {
            Long pid = row.getParentId();
            if (pid == null) {
                roots.add(row);
            } else {
                childrenByParent.computeIfAbsent(pid, k -> new ArrayList<>()).add(row);
            }
        }

        roots.sort(SCHEMA_TREE_ORDER);
        ObjectNode document = objectMapper.createObjectNode();
        appendMetadataRootsChildrenToDocument(document, roots, childrenByParent);
        try {
            return objectMapper.writeValueAsString(document);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize attributes schema JSON", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getAttributesMetadataJson(Long tenantId, String contractCode, Map<String, String> varValues) {
        AuthenticatedUser user = getCurrentUser();
        
        authorizationService.check(user, AuthZ.ResourceType.TENANT, null, tenantId, AuthZ.Action.LIST);

        List<AttributeDefEntity> rows =
                attributeDefRepository.findByTenantIdAndDocumentId(tenantId, contractCode);

        rows.removeIf(row -> !(row.getVarType().equals("IN") || row.getVarType().equals("OBJECT")));

        Map<Long, List<AttributeDefEntity>> childrenByParent = new HashMap<>();
        List<AttributeDefEntity> roots = new ArrayList<>();

        for (AttributeDefEntity row : rows) {
            Long pid = row.getParentId();
            if (pid == null) {
                roots.add(row);
            } else {
                childrenByParent.computeIfAbsent(pid, k -> new ArrayList<>()).add(row);
            }
        }

        Map<String, String> vals = varValues != null ? varValues : Map.of();
        roots.sort(SCHEMA_TREE_ORDER);
        ObjectNode document = objectMapper.createObjectNode();
        appendFilteredMetadataRootsChildrenToDocument(document, roots, childrenByParent, vals);
        try {
            return objectMapper.writeValueAsString(document);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize attributes schema JSON", ex);
        }
    }

    @Override
    @Transactional
    public void addAttribute(Long tenantId, String contractCode, LobVar lobVar) {
        AuthenticatedUser user = getCurrentUser();
        String documentId = contractCode;
        authorizationService.check(user, AuthZ.ResourceType.TENANT, null, tenantId, AuthZ.Action.MANAGE);

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
            assertNodeInSchema(lobVar.getParent_id(), tenantId, documentId);
        }

        AttributeDefEntity e = new AttributeDefEntity();
        e.setTenantId(tenantId);
        e.setDocumentId(documentId);
        e.setVarCardinality("SINGLE");
        applyLobVarToEntity(lobVar, e);
        AttributeDefEntity saved = attributeDefRepository.save(e);
        lobVar.setId(saved.getId());
    }

    @Override
    @Transactional
    public void updateAttribute(Long tenantId, String contractCode, LobVar lobVar) {
        AuthenticatedUser user = getCurrentUser();
        String documentId = contractCode;
        authorizationService.check(user, AuthZ.ResourceType.TENANT, null, tenantId, AuthZ.Action.MANAGE);

        if (lobVar == null || lobVar.getId() == null) {
            throw new BadRequestException("id is required");
        }
        assertNodeInSchema(lobVar.getId(), tenantId, documentId);

        if (lobVar.getParent_id() != null) {
            if (lobVar.getParent_id().equals(lobVar.getId())) {
                throw new BadRequestException("parent_id cannot equal id");
            }
            assertNodeInSchema(lobVar.getParent_id(), tenantId, documentId);
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
        if (!tenantId.equals(e.getTenantId()) || !documentId.equals(e.getDocumentId())) {
            throw new NotFoundException("Schema node not found: " + lobVar.getId());
        }
        applyLobVarToEntity(lobVar, e);
        attributeDefRepository.save(e);
    }

    @Override
    public Long nextId() {
        return attributeDefRepository.nextId();
    }

    @Override
    @Transactional
    public void deleteAttribute(Long tenantId, String contractCode, LobVar lobVar) {
        AuthenticatedUser user = getCurrentUser();
        String documentId = contractCode;
        authorizationService.check(user, AuthZ.ResourceType.TENANT, null, tenantId, AuthZ.Action.MANAGE);

        if (lobVar == null || lobVar.getId() == null) {
            throw new BadRequestException("id is required");
        }
        assertNodeInSchema(lobVar.getId(), tenantId, documentId);

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
                .setParameter("tid", tenantId)
                .setParameter("docId", documentId)
                .executeUpdate();
        if (n == 0) {
            throw new NotFoundException("Schema node not found: " + lobVar.getId());
        }
    }
}
