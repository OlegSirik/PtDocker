package ru.pt.api.service.db;

import ru.pt.api.dto.refdict.RefDataItemDto;
import ru.pt.api.dto.refdict.RefDictDto;

import java.util.List;

public interface RefDictAdminService {

    List<RefDictDto> listDicts(Long tid);

    RefDictDto createDict(Long tid, RefDictDto dto);

    RefDictDto updateDict(Long tid, String code, RefDictDto dto);

    void deleteDict(Long tid, String code);

    List<RefDataItemDto> listData(Long tid, String dictCode);

    RefDataItemDto createData(Long tid, String dictCode, RefDataItemDto dto);

    RefDataItemDto updateData(Long tid, String dictCode, String itemCode, RefDataItemDto dto);

    void deleteData(Long tid, String dictCode, String itemCode);

    void newTenantCreated(Long tid);

    void reloadCache();
}
