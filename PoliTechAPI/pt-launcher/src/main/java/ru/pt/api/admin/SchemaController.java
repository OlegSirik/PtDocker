package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.schema.AttributeDefDto;
import ru.pt.api.dto.schema.EntityDefDto;
import ru.pt.api.dto.schema.SectionDto;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.schema.SchemaService;
import ru.pt.auth.security.SecurityContextHelper;

import java.util.List;

/**
 * Controller for managing contract schema (sections, entities, attributes)
 * URL Pattern: /api/v1/{tenantCode}/admin/schema/{contract_code}
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/schema/{contractCode}")
public class SchemaController extends SecuredController {

    private final SchemaService schemaService;

    public SchemaController(SecurityContextHelper securityContextHelper,
                           SchemaService schemaService) {
        super(securityContextHelper);
        this.schemaService = schemaService;
    }

    private Long getCurrentTenantId() {
        return getCurrentUser().getTenantId();
    }
/* 
    @PostMapping("/newTenant/{tid}")
    public ResponseEntity<Void> createTenant (
        @PathVariable Long tid
    ) {
        schemaService.newTenantCreated(tid);
        return ResponseEntity.noContent().build(); 
    }
 */
    // ========== SECTIONS ==========

    /**
     * GET /admin/schema/{contract_code}/sections
     * Return List of sections for current tid and contract code
     */
    @GetMapping("/sections")
    public ResponseEntity<List<SectionDto>> getSections(
            @PathVariable String tenantCode,
            @PathVariable String contractCode) {
        Long tid = getCurrentTenantId();
        List<SectionDto> sections = schemaService.getSections(tid, contractCode);
        return ResponseEntity.ok(sections);
    }

    /**
     * POST /admin/schema/{contract_code}/sections
     * Insert new record. code must contains only latin letters, first char in lowercase.
     */
    @PostMapping("/sections")
    public ResponseEntity<SectionDto> createSection(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @RequestBody SectionDto dto) {
        Long tid = getCurrentTenantId();
        SectionDto created = schemaService.createSection(tid, contractCode, dto);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /admin/schema/{contract_code}/sections/{code}
     * Only name can be updated
     */
    @PutMapping("/sections/{code}")
    public ResponseEntity<SectionDto> updateSection(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @PathVariable String code,
            @RequestBody SectionDto dto) {
        Long tid = getCurrentTenantId();
        SectionDto updated = schemaService.updateSection(tid, contractCode, code, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /admin/schema/{contract_code}/sections/{code}
     * Delete and catch exception if child record exists, return error "Delete child record first"
     */
    @DeleteMapping("/sections/{code}")
    public ResponseEntity<Void> deleteSection(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @PathVariable String code) {
        Long tid = getCurrentTenantId();
        schemaService.deleteSection(tid, contractCode, code);
        return ResponseEntity.noContent().build();
    }

    // ========== ENTITIES ==========

    /**
     * GET /admin/schema/{contract_code}/{section_code}/entities
     * Return List of entities for current tid and contract code
     */
    @GetMapping("/{sectionCode}/entities")
    public ResponseEntity<List<EntityDefDto>> getEntities(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @PathVariable String sectionCode) {
        Long tid = getCurrentTenantId();
        List<EntityDefDto> entities = schemaService.getEntities(tid, contractCode, sectionCode);
        return ResponseEntity.ok(entities);
    }

    /**
     * POST /admin/schema/{contract_code}/{section_code}/entities
     * Insert new record. code must contains only latin letters, first char in lowercase.
     */
    @PostMapping("/{sectionCode}/entities")
    public ResponseEntity<EntityDefDto> createEntity(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @PathVariable String sectionCode,
            @RequestBody EntityDefDto dto) {
        Long tid = getCurrentTenantId();
        EntityDefDto created = schemaService.createEntity(tid, contractCode, sectionCode, dto);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /admin/schema/{contract_code}/{section_code}/entities/{code}
     * Only name can be updated
     */
    @PutMapping("/{sectionCode}/entities/{code}")
    public ResponseEntity<EntityDefDto> updateEntity(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @PathVariable String sectionCode,
            @PathVariable String code,
            @RequestBody EntityDefDto dto) {
        Long tid = getCurrentTenantId();
        EntityDefDto updated = schemaService.updateEntity(tid, contractCode, sectionCode, code, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /admin/schema/{contract_code}/{section_code}/entities/{code}
     * Delete and catch exception if child record exists, return error "Delete child record first"
     */
    @DeleteMapping("/{sectionCode}/entities/{code}")
    public ResponseEntity<Void> deleteEntity(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @PathVariable String sectionCode,
            @PathVariable String code) {
        Long tid = getCurrentTenantId();
        schemaService.deleteEntity(tid, contractCode, sectionCode, code);
        return ResponseEntity.noContent().build();
    }

    // ========== ATTRIBUTES ==========

    /**
     * GET /admin/schema/{contract_code}/{section_code}/{entity_code}/attributes
     * Return List of attributes for current tid and sectionId
     */
    @GetMapping("/{sectionCode}/{entityCode}/attributes")
    public ResponseEntity<List<AttributeDefDto>> getAttributes(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @PathVariable String sectionCode,
            @PathVariable String entityCode) {
        Long tid = getCurrentTenantId();
        List<AttributeDefDto> attributes = schemaService.getAttributes(tid, contractCode, sectionCode, entityCode);
        return ResponseEntity.ok(attributes);
    }

    /**
     * POST /admin/schema/{contract_code}/{section_code}/{entity_code}/attributes
     * Insert new record. code must contains only latin letters, first char in lowercase.
     */
    @PostMapping("/{sectionCode}/{entityCode}/attributes")
    public ResponseEntity<AttributeDefDto> createAttribute(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @PathVariable String sectionCode,
            @PathVariable String entityCode,
            @RequestBody AttributeDefDto dto) {
        Long tid = getCurrentTenantId();
        AttributeDefDto created = schemaService.createAttribute(tid, contractCode, sectionCode, entityCode, dto);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /admin/schema/{contract_code}/{section_code}/{entity_code}/attributes/{code}
     * Only name can be updated
     */
    @PutMapping("/{sectionCode}/{entityCode}/attributes/{code}")
    public ResponseEntity<AttributeDefDto> updateAttribute(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @PathVariable String sectionCode,
            @PathVariable String entityCode,
            @PathVariable String code,
            @RequestBody AttributeDefDto dto) {
        Long tid = getCurrentTenantId();
        AttributeDefDto updated = schemaService.updateAttribute(tid, contractCode, sectionCode, entityCode, code, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /admin/schema/{contract_code}/{section_code}/{entity_code}/attributes/{code}
     * Delete and catch exception if child record exists, return error "Delete child record first"
     */
    @DeleteMapping("/{sectionCode}/{entityCode}/attributes/{code}")
    public ResponseEntity<Void> deleteAttribute(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @PathVariable String sectionCode,
            @PathVariable String entityCode,
            @PathVariable String code) {
        Long tid = getCurrentTenantId();
        schemaService.deleteAttribute(tid, contractCode, sectionCode, entityCode, code);
        return ResponseEntity.noContent().build();
    }

}
