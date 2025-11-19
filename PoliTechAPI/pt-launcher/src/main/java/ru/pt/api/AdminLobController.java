package ru.pt.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.LobVar;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.product.LobService;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.hz.JsonExampleBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/lobs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLobController extends SecuredController {

    private final LobService lobService;

    public AdminLobController(LobService lobService) {
        this.lobService = lobService;
    }

    // get /admin/lobs return id, Code, Name from repository
    @GetMapping
    public List<Map<String, Object>> listLobs(@AuthenticationPrincipal UserDetailsImpl user) {
        requireAdmin(user);
        return lobService.listActiveSummaries().stream()
                .map(row -> Map.of(
                        "id", row[0],
                        "mpCode", row[1],
                        "mpName", row[2]
                ))
                .collect(Collectors.toList());
    }

    // get /admin/lobs/{lob_code} returns json
    @GetMapping("/{code}")
    public ResponseEntity<LobModel> getByCode(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("code") String code) {
        requireAdmin(user);
        return ResponseEntity.ok(lobService.getByCode(code));
    }

    // post /admin/lobs insert new record
    @PostMapping
    public ResponseEntity<LobModel> createLob(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody LobModel payload) {
        requireAdmin(user);
        LobModel created = lobService.create(payload);
        return ResponseEntity.ok(created);
    }

// put /admin/lobs/{lob_code} replace json, fix name and mpCode/id rules
    @PutMapping("/{code}")
    public ResponseEntity<LobModel> updateLob(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("code") String code,
            @RequestBody LobModel payload) {
        requireAdmin(user);
        return ResponseEntity.ok(lobService.updateByCode(code, payload));
    }

    // delete /admin/lobs/{lob_code} soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLob(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Integer id) {
        requireAdmin(user);
        boolean deleted = lobService.softDeleteById(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // get /admin/lobs/example returns json example
    @GetMapping("/{code}/example")
    public ResponseEntity<String> getJsonExample(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("code") String code) {
        requireAdmin(user);
        LobModel lob = lobService.getByCode(code);
        if (lob == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            String jsonExample = JsonExampleBuilder.buildJsonExample(
                    lob.getMpVars().stream()
                    .map(LobVar::getVarPath)
                    .collect(Collectors.toList())
            );
            return ResponseEntity.ok(jsonExample);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


}


