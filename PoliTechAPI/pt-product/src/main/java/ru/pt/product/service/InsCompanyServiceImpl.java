package ru.pt.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.InsuranceCompanyDto;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.product.InsCompanyService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.InsuranceCompanyEntity;
import ru.pt.product.repository.InsuranceCompanyRepository;
import ru.pt.product.utils.InsuranceCompanyMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsCompanyServiceImpl implements InsCompanyService {

    private static final String DEFAULT_STATUS = "active";

    private final InsuranceCompanyRepository repository;
    private final SecurityContextHelper securityContextHelper;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public InsuranceCompanyDto create(InsuranceCompanyDto dto) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.INS_COMPANY, null, null, AuthZ.Action.MANAGE);
        Long tid = getCurrentTenantId();
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new BadRequestException("code must not be empty");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("name must not be empty");
        }
        String code = dto.getCode().trim();
        if (repository.existsByTidAndCode(tid, code)) {
            throw new BadRequestException("Insurance company with code already exists: " + code);
        }
        InsuranceCompanyEntity e = new InsuranceCompanyEntity();
        e.setTid(tid);
        InsuranceCompanyMapper.applyDtoToEntity(dto, e);
        e.setCode(code);
        e.setStatus(normalizeStatus(dto.getStatus()));
        e = repository.save(e);
        return InsuranceCompanyMapper.toDto(e);
    }

    @Override
    @Transactional
    public InsuranceCompanyDto update(InsuranceCompanyDto dto) {
        if (dto.getId() == null) {
            throw new BadRequestException("id must not be null for update");
        }
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.INS_COMPANY, String.valueOf(dto.getId()), null, AuthZ.Action.MANAGE);
        Long tid = getCurrentTenantId();
        InsuranceCompanyEntity e = repository.findByTidAndId(tid, dto.getId())
                .orElseThrow(() -> new NotFoundException("Insurance company not found: " + dto.getId()));
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new BadRequestException("code must not be empty");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("name must not be empty");
        }
        String code = dto.getCode().trim();
        if (!code.equals(e.getCode()) && repository.existsByTidAndCode(tid, code)) {
            throw new BadRequestException("Insurance company with code already exists: " + code);
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            e.setStatus(normalizeStatus(dto.getStatus()));
        }
        InsuranceCompanyMapper.applyDtoToEntity(dto, e);
        e.setCode(code);
        e = repository.save(e);
        return InsuranceCompanyMapper.toDto(e);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.INS_COMPANY, String.valueOf(id), null, AuthZ.Action.MANAGE);
        Long tid = getCurrentTenantId();
        InsuranceCompanyEntity e = repository.findByTidAndId(tid, id)
                .orElseThrow(() -> new NotFoundException("Insurance company not found: " + id));
        repository.delete(e);
    }

    @Override
    public InsuranceCompanyDto get(Long id) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.INS_COMPANY, String.valueOf(id), null, AuthZ.Action.VIEW);
        Long tid = getCurrentTenantId();
        InsuranceCompanyEntity e = repository.findByTidAndId(tid, id)
                .orElseThrow(() -> new NotFoundException("Insurance company not found: " + id));
        return InsuranceCompanyMapper.toDto(e);
    }

    @Override
    public List<InsuranceCompanyDto> getAll() {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.INS_COMPANY, null, null, AuthZ.Action.LIST);
        Long tid = getCurrentTenantId();
        return repository.findByTidOrderByCodeAsc(tid).stream()
                .map(InsuranceCompanyMapper::toDto)
                .collect(Collectors.toList());
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return DEFAULT_STATUS;
        }
        String s = status.trim().toLowerCase();
        if (!s.equals("active") && !s.equals("suspended")) {
            throw new BadRequestException("status must be 'active' or 'suspended'");
        }
        return s;
    }

    private ru.pt.api.security.AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
    }

    private Long getCurrentTenantId() {
        return getCurrentUser().getTenantId();
    }
}
