package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.LobVar;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.product.LobService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.hz.JsonExampleBuilder;

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
//@PreAuthorize("hasRole('SYS_ADMIN')")
public class LobController extends SecuredController {

    private final LobService lobService;

    public LobController(LobService lobService,
                         SecurityContextHelper securityContextHelper) {
        super(securityContextHelper);
        this.lobService = lobService;
    }

    // get /admin/lobs return id, Code, Name from repository
    @GetMapping
    public List<LobModel> listLobs(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user) {

        return lobService.listActiveSummaries();
    }

    // get /admin/lobs/{lob_code} returns json
    @GetMapping("/{id}")
    public ResponseEntity<LobModel> getByCode(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") String id) {
        
        LobModel lob;
        try {
            Integer lobId = Integer.parseInt(id);
            lob = lobService.getById(lobId);
        } catch (NumberFormatException e) {
            lob = lobService.getByCode(id);
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
       
        LobModel created = lobService.create(payload);
        return ResponseEntity.ok(created);
    }

    // put /admin/lobs/{lob_code} replace json, fix name and mpCode/id rules
    @PutMapping("/{id}")
    public ResponseEntity<LobModel> updateLob(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Integer id,
            @RequestBody LobModel payload) {

        if (payload.getId() == null || id.longValue() != payload.getId()) {
            throw new BadRequestException("ID in path must match payload ID");
        }
        
        return ResponseEntity.ok(lobService.update(payload));
    }

    // delete /admin/lobs/{lob_code} soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLob(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Integer id) {
        //requireAdmin(user);
        boolean deleted = lobService.softDeleteById(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // get /admin/lobs/example returns json example
    @GetMapping("/{code}/example")
    public ResponseEntity<String> getJsonExample(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("code") String code) {
        //requireAdmin(user);
        LobModel lob = lobService.getByCode(code);
        if (lob == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            List<String> varPaths = lob.getMpVars() != null
                    ? lob.getMpVars().stream().map(LobVar::getVarPath).collect(Collectors.toList())
                    : List.of();
            String jsonExample = JsonExampleBuilder.buildJsonExample(varPaths);
            return ResponseEntity.ok(jsonExample);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}


