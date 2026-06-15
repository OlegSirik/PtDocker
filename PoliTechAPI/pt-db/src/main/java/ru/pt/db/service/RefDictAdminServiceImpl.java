package ru.pt.db.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.refdict.RefDataItemDto;
import ru.pt.api.dto.refdict.RefDictDto;
import ru.pt.api.service.db.RefDictAdminService;
import ru.pt.db.entity.RefDataEntity;
import ru.pt.db.entity.RefDictEntity;
import ru.pt.db.repository.RefDataRepository;
import ru.pt.db.repository.RefDictRepository;

import java.util.List;

@Service
public class RefDictAdminServiceImpl implements RefDictAdminService {

    private static final long TEMPLATE_TID = 1L;

    private final RefDictRepository refDictRepository;
    private final RefDataRepository refDataRepository;
    private final RefDataService refDataService;

    public RefDictAdminServiceImpl(
            RefDictRepository refDictRepository,
            RefDataRepository refDataRepository,
            RefDataService refDataService) {
        this.refDictRepository = refDictRepository;
        this.refDataRepository = refDataRepository;
        this.refDataService = refDataService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefDictDto> listDicts(Long tid) {
        return refDictRepository.findByTidOrderByCodeAsc(tid).stream()
                .map(this::toDictDto)
                .toList();
    }

    @Override
    @Transactional
    public RefDictDto createDict(Long tid, RefDictDto dto) {
        validateDictDto(dto);
        if (refDictRepository.existsByTidAndCode(tid, dto.getCode())) {
            throw new BadRequestException("Dictionary already exists: " + dto.getCode());
        }
        RefDictEntity entity = new RefDictEntity();
        entity.setTid(tid);
        entity.setCode(dto.getCode().trim());
        entity.setName(dto.getName().trim());
        refDictRepository.save(entity);
        refDataService.reloadCache();
        return toDictDto(entity);
    }

    @Override
    @Transactional
    public RefDictDto updateDict(Long tid, String code, RefDictDto dto) {
        RefDictEntity entity = findDict(tid, code);
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Dictionary name is required");
        }
        entity.setName(dto.getName().trim());
        refDictRepository.save(entity);
        refDataService.reloadCache();
        return toDictDto(entity);
    }

    @Override
    @Transactional
    public void deleteDict(Long tid, String code) {
        RefDictEntity entity = findDict(tid, code);
        refDataRepository.findByTidAndRefCodeOrderByMdCodeAsc(tid, code)
                .forEach(refDataRepository::delete);
        refDictRepository.delete(entity);
        refDataService.reloadCache();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefDataItemDto> listData(Long tid, String dictCode) {
        findDict(tid, dictCode);
        return refDataRepository.findByTidAndRefCodeOrderByMdCodeAsc(tid, dictCode).stream()
                .map(this::toDataDto)
                .toList();
    }

    @Override
    @Transactional
    public RefDataItemDto createData(Long tid, String dictCode, RefDataItemDto dto) {
        validateDataDto(dto);
        findDict(tid, dictCode);
        if (refDataRepository.existsByTidAndRefCodeAndMdCode(tid, dictCode, dto.getCode())) {
            throw new BadRequestException("Reference item already exists: " + dto.getCode());
        }
        RefDataEntity entity = new RefDataEntity();
        entity.setTid(tid);
        entity.setRefCode(dictCode);
        entity.setMdCode(dto.getCode().trim());
        entity.setMdName(dto.getName().trim());
        refDataRepository.save(entity);
        refDataService.reloadCache();
        return toDataDto(entity);
    }

    @Override
    @Transactional
    public RefDataItemDto updateData(Long tid, String dictCode, String itemCode, RefDataItemDto dto) {
        RefDataEntity entity = findData(tid, dictCode, itemCode);
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Reference item name is required");
        }
        entity.setMdName(dto.getName().trim());
        refDataRepository.save(entity);
        refDataService.reloadCache();
        return toDataDto(entity);
    }

    @Override
    @Transactional
    public void deleteData(Long tid, String dictCode, String itemCode) {
        RefDataEntity entity = findData(tid, dictCode, itemCode);
        refDataRepository.delete(entity);
        refDataService.reloadCache();
    }

    @Override
    @Transactional
    public void newTenantCreated(Long tid) {
        if (tid == null || tid.equals(TEMPLATE_TID)) {
            return;
        }

        List<RefDictEntity> templateDicts = refDictRepository.findByTidOrderByCodeAsc(TEMPLATE_TID);
        for (RefDictEntity templateDict : templateDicts) {
            if (refDictRepository.existsByTidAndCode(tid, templateDict.getCode())) {
                continue;
            }
            RefDictEntity dict = new RefDictEntity();
            dict.setTid(tid);
            dict.setCode(templateDict.getCode());
            dict.setName(templateDict.getName());
            refDictRepository.save(dict);

            List<RefDataEntity> templateItems = refDataRepository.findByTidAndRefCodeOrderByMdCodeAsc(
                    TEMPLATE_TID, templateDict.getCode());
            for (RefDataEntity templateItem : templateItems) {
                RefDataEntity item = new RefDataEntity();
                item.setTid(tid);
                item.setRefCode(templateItem.getRefCode());
                item.setMdCode(templateItem.getMdCode());
                item.setMdName(templateItem.getMdName());
                refDataRepository.save(item);
            }
        }
        refDataService.reloadCache();
    }

    @Override
    public void reloadCache() {
        refDataService.reloadCache();
    }

    private RefDictEntity findDict(Long tid, String code) {
        return refDictRepository.findById(new RefDictEntity.RefDictId(tid, code))
                .orElseThrow(() -> new NotFoundException("Dictionary not found: " + code));
    }

    private RefDataEntity findData(Long tid, String dictCode, String itemCode) {
        return refDataRepository.findById(new RefDataEntity.RefDataId(tid, dictCode, itemCode))
                .orElseThrow(() -> new NotFoundException("Reference item not found: " + itemCode));
    }

    private static void validateDictDto(RefDictDto dto) {
        if (dto == null || dto.getCode() == null || dto.getCode().isBlank()) {
            throw new BadRequestException("Dictionary code is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Dictionary name is required");
        }
    }

    private static void validateDataDto(RefDataItemDto dto) {
        if (dto == null || dto.getCode() == null || dto.getCode().isBlank()) {
            throw new BadRequestException("Reference item code is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Reference item name is required");
        }
    }

    private RefDictDto toDictDto(RefDictEntity entity) {
        return new RefDictDto(entity.getCode(), entity.getName());
    }

    private RefDataItemDto toDataDto(RefDataEntity entity) {
        return new RefDataItemDto(entity.getMdCode(), entity.getMdName());
    }
}
