package ru.pt.api;

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
import ru.pt.auth.security.UserDetailsImpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/files")
public class AdminFileController extends SecuredController {

    private final FileService fileService;

    public AdminFileController(FileService fileService) {
        this.fileService = fileService;
    }

    // POST /admin/files body json
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> createMeta(
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

    // POST /admin/files/{id} multipart file upload
    @PostMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Long id,
            @RequestPart("file") MultipartFile file) {
        requireAdmin(user);
        try {
            fileService.uploadBody(id, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.noContent().build();
    }

    // DELETE /admin/files/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Long id) {
        requireAdmin(user);
        fileService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // GET /admin/files?product_code=***
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(value = "product_code", required = false) String productCode) {
        requireAdmin(user);
        return ResponseEntity.ok(fileService.list(productCode));
    }

    // GET /admin/files/{id} -> file body
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Long id) {
        requireAdmin(user);
        byte[] bytes = fileService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=output.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // POST /admin/files/{ID}/cmd/process
    @PostMapping("/{id}/cmd/process")
    public ResponseEntity<byte[]> process(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Long id,
            @RequestBody List<Map<String, String>> pairs) {
        requireAdmin(user);
        java.util.Map<String, String> kv = new java.util.HashMap<>();
        for (Map<String, String> p : pairs) {
            kv.put(p.get("varCode"), p.get("varValue"));
        }
        byte[] bytes = fileService.process(id, kv);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processed.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}


