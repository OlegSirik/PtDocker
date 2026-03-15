package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import ru.pt.api.dto.file.FileDownload;
import ru.pt.api.service.file.FileService;
import ru.pt.auth.security.UserDetailsImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Контроллер для управления файлами
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/files
 * tenantCode: pt, vsk, msg
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/files")
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadFile(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestPart("file") MultipartFile file) throws IOException {

        Long fileId = fileService.uploadFile(
                tenantCode,
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize());
        return ResponseEntity.ok(Map.of("id", String.valueOf(fileId)));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("fileId") Long fileId) {
        //requireAdmin(user);
        fileService.delete(fileId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{fileId}")
    public ResponseEntity<StreamingResponseBody> download(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("fileId") Long fileId) {
        FileDownload fd = fileService.downloadFile(fileId);

        StreamingResponseBody body = outputStream -> {

            try (InputStream input = fd.inputStream()) {
                input.transferTo(outputStream);
            }
    
        };
    
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fd.contentType()))
                .contentLength(fd.size())
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fd.filename() + "\""
                )
                .body(body);
    }



}


