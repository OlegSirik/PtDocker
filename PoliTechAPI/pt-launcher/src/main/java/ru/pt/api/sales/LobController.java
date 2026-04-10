package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.LobVar;
import ru.pt.api.service.product.LobService;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.hz.JsonExampleBuilder123;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для управления LOB (Line of Business)
 * Доступен только для SYS_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/lobs
 * tenantCode: pt, vsk, msg
 */
@RestController
@RequestMapping("/api/v1/{tenantCode}/admin/lobs")
@SecurityRequirement(name = "bearerAuth")

public class LobController {

    private final LobService lobService;

    public LobController(LobService lobService) {
        this.lobService = lobService;
    }

    // get /admin/lobs return id, Code, Name from repository
    @GetMapping
    public List<LobModel> listLobs(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user) {

        return lobService.listActiveSummaries(user.getTenantId());
    }

    // get /admin/lobs/{lob_code} returns json
    @GetMapping("/{id}")
    public ResponseEntity<LobModel> getByCode(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") String id) {
        
        LobModel lob;
        try {
            Long lobId = Long.parseLong(id);
            lob = lobService.getById(user.getTenantId(), lobId);
        } catch (NumberFormatException e) {
            lob = lobService.getByCode(user.getTenantId(), id);
        }
        
        if (lob == null) {
            throw new NotFoundException("Lob " + id + " не найден");
        }
        
        return ResponseEntity.ok(lob);
    }

    // post /admin/lobs insert new record
    @PostMapping
    public ResponseEntity<LobModel> createLob(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody LobModel payload) {
       
        LobModel created = lobService.create(user.getTenantId(), payload);
        return ResponseEntity.ok(created);
    }

    // put /admin/lobs/{lob_code} replace json, fix name and mpCode/id rules
    @PutMapping("/{id}")
    public ResponseEntity<LobModel> updateLob(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Long id,
            @RequestBody LobModel payload) {

        if (payload.getId() == null || !id.equals(payload.getId())) {
            throw new BadRequestException("ID in path must match payload ID");
        }
        
        return ResponseEntity.ok(lobService.update(user.getTenantId(), payload));
    }

    // delete /admin/lobs/{lob_code} soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLob(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Long id) {
        
        boolean deleted = lobService.softDeleteById(user.getTenantId(), id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // get /admin/lobs/example returns json example
    @GetMapping("/{code}/example")
    public ResponseEntity<String> getJsonExample(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("code") String code) {
                
        LobModel lob = lobService.getByCode(user.getTenantId(), code);
        if (lob == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            List<String> varPaths = lob.getMpVars() != null
                    ? lob.getMpVars().stream().map(LobVar::getVarPath).collect(Collectors.toList())
                    : List.of();
            String jsonExample = JsonExampleBuilder123.buildJsonExample(varPaths);
            return ResponseEntity.ok(jsonExample);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}


