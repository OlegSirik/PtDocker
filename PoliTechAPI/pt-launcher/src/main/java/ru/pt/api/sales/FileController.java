package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.pt.api.dto.file.FileModel;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.file.FileService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.service.TenantService;
import ru.pt.auth.entity.TenantEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления файлами
 * Доступен для SYS_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/files
 * tenantCode: pt, vsk, msg
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/files")
public class FileController extends SecuredController {

    private final FileService fileService;
    private final TenantService tenantService;

    public FileController(FileService fileService,
                          SecurityContextHelper securityContextHelper,
                          TenantService tenantService) {
        super(securityContextHelper);
        this.fileService = fileService;
        this.tenantService = tenantService;
    }

    // POST /api/v1/{tenantCode}/admin/files body json
/*     
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<Map<String, String>> createMeta(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody Map<String, String> body) {
        requireAdmin(user);
        String fileType = body.get("fileType").toLowerCase();
        String fileDesc = body.get("fileDescription");
        String productCode = body.get("productCode");
        Integer packageCode = Integer.parseInt(body.get("packageCode"));
        FileModel e = fileService.createMeta(fileType, fileDesc, productCode, packageCode);

        body.put("id", String.valueOf(e.getId()));
        return ResponseEntity.ok(body);
    }
*/
    @PostMapping
    //@PreAuthorize("hasRole('SYS_ADMIN')")  ProductAdmin it shoud be
    public ResponseEntity<Map<String, String>> uploadFile(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestPart("file") MultipartFile file) {
        requireAdmin(user);
        TenantEntity tenant = tenantService.findByCode(tenantCode)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        try {
            Long fileId = fileService.uploadFile(tenant.getId(), file.getBytes());
            return ResponseEntity.ok(Map.of("id", String.valueOf(fileId)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

// POST /api/v1/{tenantCode}/admin/files/{fileId} multipart file upload
    //@PostMapping(path = "/{fileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("fileId") Long fileId,
            @RequestPart("file") MultipartFile file) {
        requireAdmin(user);
        try {
            fileService.uploadBody(fileId, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/v1/{tenantCode}/admin/files/{fileId}
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("fileId") Long fileId) {
        requireAdmin(user);
        fileService.softDelete(fileId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/{tenantCode}/admin/files?product_code=***
    //@GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(value = "product_code", required = false) String productCode) {
        requireAdmin(user);
        return ResponseEntity.ok(fileService.list(productCode));
    }

    // GET /api/v1/{tenantCode}/admin/files/{fileId} -> file body
    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> download(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("fileId") Long fileId) {
        requireAdmin(user);
        byte[] bytes = fileService.download(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=output.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // POST /api/v1/{tenantCode}/admin/files/{fileId}/cmd/process
    //@PostMapping("/{fileId}/cmd/process")
    public ResponseEntity<byte[]> process(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("fileId") Long fileId,
            @RequestBody List<Map<String, String>> pairs) {
        requireAdmin(user);
        java.util.Map<String, String> kv = new java.util.HashMap<>();
        for (Map<String, String> p : pairs) {
            kv.put(p.get("varCode"), p.get("varValue"));
        }
        byte[] bytes = fileService.process(fileId, kv);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processed.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}


