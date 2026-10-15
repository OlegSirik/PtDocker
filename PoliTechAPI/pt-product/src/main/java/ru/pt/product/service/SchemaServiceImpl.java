package ru.pt.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.ContractModelDto;
import ru.pt.api.dto.product.LobVar;
import ru.pt.api.dto.product.VarDataType;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.schema.SchemaService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.AttributeDefEntity;
import ru.pt.product.entity.ContractModelEntity;
import ru.pt.product.repository.AttributeDefRepository;
import ru.pt.product.repository.ContractModelRepository;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
    /** {@link ObjectProvider} — без цикла конструктора с {@link LobServiceImpl} (он зависит от SchemaService). */
    private final ObjectProvider<LobService> lobService;
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

    /**
     * Metadata JSON собирается по {@code var_path} (JsonPath), не по {@code var_code} и не по дереву
     * {@code parent_id}. Фильтры {@code [?()]} / {@code [*]} / {@code []} для записи скелета
     * сводятся к {@code [0]}.
     */
    private static String normalizePathForSkeleton(String varPath) {
        if (varPath == null) {
            return "";
        }
        String s = varPath.trim();
        if (s.isEmpty() || "*".equals(s) || "null".equalsIgnoreCase(s)) {
            return "";
        }
        if (s.startsWith("$.")) {
            s = s.substring(2);
        } else if (s.startsWith("$")) {
            s = s.substring(1);
            if (s.startsWith(".")) {
                s = s.substring(1);
            }
        }
        s = s.replaceAll("\\[\\?\\([^\\]]*\\)\\]", "[0]");
        s = s.replace("[*]", "[0]");
        s = s.replace("[]", "[0]");
        return s.trim();
    }

    private record PathSeg(String name, int index, boolean arrayIndex) {
        static PathSeg prop(String name) {
            return new PathSeg(name, -1, false);
        }

        static PathSeg index(int index) {
            return new PathSeg(null, index, true);
        }
    }

    private static List<PathSeg> parseNormalizedPath(String path) {
        List<PathSeg> segs = new ArrayList<>();
        int i = 0;
        while (i < path.length()) {
            char ch = path.charAt(i);
            if (ch == '.') {
                i++;
                continue;
            }
            if (ch == '[') {
                int end = path.indexOf(']', i);
                if (end < 0) {
                    break;
                }
                String inside = path.substring(i + 1, end).trim();
                try {
                    segs.add(PathSeg.index(Integer.parseInt(inside)));
                } catch (NumberFormatException ignored) {
                    return List.of();
                }
                i = end + 1;
                continue;
            }
            int start = i;
            while (i < path.length()) {
                char c = path.charAt(i);
                if (c == '.' || c == '[') {
                    break;
                }
                i++;
            }
            String name = path.substring(start, i).trim();
            if (!name.isEmpty()) {
                segs.add(PathSeg.prop(name));
            }
        }
        return segs;
    }

    /** Запись значения по нормализованному пути; создаёт промежуточные object/array. */
    private void putAtPath(ObjectNode root, String normalizedPath, JsonNode value) {
        List<PathSeg> segs = parseNormalizedPath(normalizedPath);
        if (segs.isEmpty()) {
            return;
        }
        JsonNode cursor = root;
        for (int i = 0; i < segs.size() - 1; i++) {
            PathSeg seg = segs.get(i);
            PathSeg next = segs.get(i + 1);
            if (seg.arrayIndex()) {
                if (!(cursor instanceof ArrayNode arr)) {
                    return;
                }
                while (arr.size() <= seg.index()) {
                    arr.addNull();
                }
                JsonNode el = arr.get(seg.index());
                if (next.arrayIndex()) {
                    if (!(el instanceof ArrayNode)) {
                        el = objectMapper.createArrayNode();
                        arr.set(seg.index(), el);
                    }
                } else if (!(el instanceof ObjectNode)) {
                    el = objectMapper.createObjectNode();
                    arr.set(seg.index(), el);
                }
                cursor = el;
            } else {
                if (!(cursor instanceof ObjectNode obj)) {
                    return;
                }
                JsonNode child = obj.get(seg.name());
                if (next.arrayIndex()) {
                    if (!(child instanceof ArrayNode)) {
                        child = objectMapper.createArrayNode();
                        obj.set(seg.name(), child);
                    }
                } else if (!(child instanceof ObjectNode)) {
                    child = objectMapper.createObjectNode();
                    obj.set(seg.name(), child);
                }
                cursor = child;
            }
        }
        PathSeg last = segs.get(segs.size() - 1);
        if (last.arrayIndex()) {
            if (!(cursor instanceof ArrayNode arr)) {
                return;
            }
            while (arr.size() <= last.index()) {
                arr.addNull();
            }
            arr.set(last.index(), value);
        } else if (cursor instanceof ObjectNode obj) {
            obj.set(last.name(), value);
        }
    }

    /** Заглушка для листа метаданных. */
    private JsonNode leafTemplateNode(AttributeDefEntity e) {
        JsonNodeFactory f = objectMapper.getNodeFactory();
        if (e.getVarValue() != null && !e.getVarValue().isBlank()) {
            return f.textNode(e.getVarValue());
        }
        String dt = e.getVarDataType() != null ? e.getVarDataType().trim().toUpperCase(Locale.ROOT) : "";
        if ("NUMBER".equals(dt)) {
            return f.numberNode(BigDecimal.ZERO);
        }
        if ("BOOLEAN".equals(dt)) {
            return f.booleanNode(false);
        }
        return f.textNode("");
    }

    /** Карта значений для metadata: ключ = {@code var_path} (сырой или нормализованный), не {@code var_code}. */
    private static String lookupByVarPath(AttributeDefEntity e, Map<String, String> varValues) {
        if (varValues == null || varValues.isEmpty()) {
            return null;
        }
        String raw = e.getVarPath();
        if (raw != null) {
            String trimmed = raw.trim();
            if (!trimmed.isEmpty() && varValues.containsKey(trimmed)) {
                return varValues.get(trimmed);
            }
        }
        String norm = normalizePathForSkeleton(raw);
        if (!norm.isEmpty() && varValues.containsKey(norm)) {
            return varValues.get(norm);
        }
        return null;
    }

    private static boolean keepByFilter(AttributeDefEntity e, Map<String, String> varValues) {
        return e.isSystem() || lookupByVarPath(e, varValues) != null;
    }

    private static boolean isSchemaLeaf(
            AttributeDefEntity e, Map<Long, List<AttributeDefEntity>> childrenByParent) {
        return childrenByParent.getOrDefault(e.getId(), List.of()).isEmpty();
    }

    private JsonNode leafValueNode(AttributeDefEntity e, Map<String, String> varValuesOrNull) {
        if (varValuesOrNull == null) {
            return leafTemplateNode(e);
        }
        String fromMap = lookupByVarPath(e, varValuesOrNull);
        if (fromMap != null) {
            return objectMapper.getNodeFactory().textNode(fromMap);
        }
        return objectMapper.getNodeFactory().textNode(e.getVarValue() != null ? e.getVarValue() : "");
    }

    /** Порядок полей в metadata JSON: {@code var_ord}, затем {@code id}. */
    private static final Comparator<AttributeDefEntity> METADATA_VAR_ORD =
            Comparator
                    .comparing((AttributeDefEntity e) -> e.getVarOrd() != null ? e.getVarOrd() : Long.MAX_VALUE)
                    .thenComparing(AttributeDefEntity::getId, Comparator.nullsLast(Long::compareTo));

    /**
     * Скелет / пример JSON: листья {@code IN} кладутся по нормализованному {@code var_path}
     * в порядке {@code var_ord} (порядок ключей в ObjectNode = порядок вставки).
     * {@code varValuesOrNull == null} — полный скелет; иначе system + пути из карты
     * (ключи карты — JsonPath / {@code var_path}, не {@code var_code}).
     */
    private ObjectNode buildMetadataDocumentByJsonPath(
            List<AttributeDefEntity> rows, Map<String, String> varValuesOrNull) {
        Map<Long, List<AttributeDefEntity>> childrenByParent = new HashMap<>();
        for (AttributeDefEntity row : rows) {
            Long pid = row.getParentId();
            if (pid != null) {
                childrenByParent.computeIfAbsent(pid, k -> new ArrayList<>()).add(row);
            }
        }

        List<AttributeDefEntity> leaves = new ArrayList<>();
        for (AttributeDefEntity e : rows) {
            String type = e.getVarType() != null ? e.getVarType().trim() : "";
            if (!"IN".equals(type)) {
                continue;
            }
            if (!isSchemaLeaf(e, childrenByParent)) {
                continue;
            }
            if (varValuesOrNull != null && !keepByFilter(e, varValuesOrNull)) {
                continue;
            }
            String path = normalizePathForSkeleton(e.getVarPath());
            if (path.isEmpty()) {
                continue;
            }
            leaves.add(e);
        }
        leaves.sort(METADATA_VAR_ORD);

        ObjectNode document = objectMapper.createObjectNode();
        for (AttributeDefEntity e : leaves) {
            putAtPath(
                    document,
                    normalizePathForSkeleton(e.getVarPath()),
                    leafValueNode(e, varValuesOrNull));
        }
        return document;
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
                .varValue(e.getVarValue() != null ? e.getVarValue() : "")
                .isDeleted(false)
                // API-совместимость: name = varName (колонки name в mt_attribute_def больше нет)
                .name(e.getVarName())
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
        e.setSystem(v.getIsSystem());
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
        n.setVarDataType(t.getVarDataType());
        n.setVarValue(t.getVarValue());
        n.setVarList(t.getVarList());
        n.setSystem(t.isSystem());
        return n;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractModelDto> listContractModels(Long tenantId) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.TENANT, null, tenantId, AuthZ.Action.LIST);

        return contractModelRepository.findByTid(tenantId).stream()
                .sorted(Comparator
                        .comparing((ContractModelEntity m) -> m.getName() != null ? m.getName() : "", String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(m -> m.getCode() != null ? m.getCode() : "", String.CASE_INSENSITIVE_ORDER))
                .map(m -> new ContractModelDto(m.getId(), m.getCode(), m.getName()))
                .collect(Collectors.toList());
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

        // OBJECT нужны только чтобы отличить листья IN (есть ли дети в дереве схемы)
        rows.removeIf(row -> {
            String t = row.getVarType();
            return t == null || (!t.equals("IN") && !t.equals("OBJECT"));
        });

        ObjectNode document = buildMetadataDocumentByJsonPath(rows, null);
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

        rows.removeIf(row -> {
            String t = row.getVarType();
            return t == null || (!t.equals("IN") && !t.equals("OBJECT"));
        });

        Map<String, String> vals = varValues != null ? varValues : Map.of();
        ObjectNode document = buildMetadataDocumentByJsonPath(rows, vals);
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
        applyLobVarToEntity(lobVar, e);
        AttributeDefEntity saved = attributeDefRepository.save(e);
        lobVar.setId(saved.getId());

        lobService.getObject().appendSchemaAttributeAsDeleted(tenantId, lobVar);
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
