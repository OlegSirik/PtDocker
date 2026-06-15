package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.refdict.RefDataItemDto;
import ru.pt.api.dto.refdict.RefDictDto;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.db.RefDictAdminService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/refdicts")
public class RefDictManagementController extends SecuredController {

    private final RefDictAdminService refDictAdminService;

    public RefDictManagementController(
            SecurityContextHelper securityContextHelper,
            RefDictAdminService refDictAdminService) {
        super(securityContextHelper);
        this.refDictAdminService = refDictAdminService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<List<RefDictDto>> listDicts() {
        UserDetailsImpl user = getCurrentUser();
        return ResponseEntity.ok(refDictAdminService.listDicts(user.getTenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<RefDictDto> createDict(@RequestBody RefDictDto dto) {
        UserDetailsImpl user = getCurrentUser();
        return ResponseEntity.ok(refDictAdminService.createDict(user.getTenantId(), dto));
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<RefDictDto> updateDict(@PathVariable String code, @RequestBody RefDictDto dto) {
        UserDetailsImpl user = getCurrentUser();
        return ResponseEntity.ok(refDictAdminService.updateDict(user.getTenantId(), code, dto));
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<Void> deleteDict(@PathVariable String code) {
        UserDetailsImpl user = getCurrentUser();
        refDictAdminService.deleteDict(user.getTenantId(), code);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{code}/items")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<List<RefDataItemDto>> listItems(@PathVariable String code) {
        UserDetailsImpl user = getCurrentUser();
        return ResponseEntity.ok(refDictAdminService.listData(user.getTenantId(), code));
    }

    @PostMapping("/{code}/items")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<RefDataItemDto> createItem(@PathVariable String code, @RequestBody RefDataItemDto dto) {
        UserDetailsImpl user = getCurrentUser();
        return ResponseEntity.ok(refDictAdminService.createData(user.getTenantId(), code, dto));
    }

    @PutMapping("/{code}/items/{itemCode}")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<RefDataItemDto> updateItem(
            @PathVariable String code,
            @PathVariable String itemCode,
            @RequestBody RefDataItemDto dto) {
        UserDetailsImpl user = getCurrentUser();
        return ResponseEntity.ok(refDictAdminService.updateData(user.getTenantId(), code, itemCode, dto));
    }

    @DeleteMapping("/{code}/items/{itemCode}")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<Void> deleteItem(@PathVariable String code, @PathVariable String itemCode) {
        UserDetailsImpl user = getCurrentUser();
        refDictAdminService.deleteData(user.getTenantId(), code, itemCode);
        return ResponseEntity.noContent().build();
    }
}
