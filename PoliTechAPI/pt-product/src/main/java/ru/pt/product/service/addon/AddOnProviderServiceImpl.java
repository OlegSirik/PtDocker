package ru.pt.product.service.addon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pt.api.dto.addon.ProviderDto;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.refs.RecordStatus;
import ru.pt.api.service.addon.AddOnProviderService;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.AddonProviderEntity;
import ru.pt.product.repository.AddonProviderRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddOnProviderServiceImpl implements AddOnProviderService {

    private final AddonProviderRepository repository;
    private final SecurityContextHelper securityContextHelper;
    private final AuthorizationService authorizationService;

    @Override
    public ProviderDto createProvider(ProviderDto cmd) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.POLICY_ADDON, null, null, AuthZ.Action.MANAGE);
        Long tid = getCurrentTenantId();
        var entity = new AddonProviderEntity();
        entity.setTid(tid);
        entity.setName(cmd.getName());
        entity.setRecordStatus(cmd.getRecordStatus() != null ? cmd.getRecordStatus().getValue() : RecordStatus.ACTIVE.getValue());
        entity.setExecutionMode(cmd.getExecutionMode() != null ? cmd.getExecutionMode() : "LOCAL");
        entity = repository.save(entity);
        return toDto(entity);
    }

    @Override
    public ProviderDto updateProvider(ProviderDto cmd) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.POLICY_ADDON, cmd.getId() != null ? String.valueOf(cmd.getId()) : null, null, AuthZ.Action.MANAGE);
        if (cmd.getId() == null) {
            throw new BadRequestException("Provider ID must not be null for update");
        }
        Long tid = getCurrentTenantId();
        var entity = repository.findByTidAndId(tid, cmd.getId())
                .orElseThrow(() -> new NotFoundException("Provider not found: " + cmd.getId()));
        entity.setName(cmd.getName());
        entity.setRecordStatus(cmd.getRecordStatus().getValue());
        entity.setExecutionMode(cmd.getExecutionMode());
        entity = repository.save(entity);
        return toDto(entity);
    }

    @Override
    public void suspendProvider(Long id) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.POLICY_ADDON, String.valueOf(id), null, AuthZ.Action.MANAGE);
        Long tid = getCurrentTenantId();
        var entity = repository.findByTidAndId(tid, id)
                .orElseThrow(() -> new NotFoundException("Provider not found: " + id));
        entity.setRecordStatus(RecordStatus.SUSPENDED.toValue());
        repository.save(entity);
    }

    @Override
    public List<ProviderDto> getProviders() {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.POLICY_ADDON, null, null, AuthZ.Action.LIST);
        Long tid = getCurrentTenantId();
        return repository.findByTidOrderByName(tid).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProviderDto getProvider(Long id) {
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.POLICY_ADDON, String.valueOf(id), null, AuthZ.Action.VIEW);
        Long tid = getCurrentTenantId();
        var entity = repository.findByTidAndId(tid, id)
                .orElseThrow(() -> new NotFoundException("Provider not found: " + id));
        return toDto(entity);
    }

    private ru.pt.api.security.AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
    }

    private Long getCurrentTenantId() {
        return getCurrentUser().getTenantId();
    }

    private ProviderDto toDto(AddonProviderEntity e) {
        return ProviderDto.builder()
                .id(e.getId())
                .name(e.getName())
                .recordStatus(RecordStatus.fromValue(e.getRecordStatus()))
                .executionMode(e.getExecutionMode())
                .build();
    }

}
