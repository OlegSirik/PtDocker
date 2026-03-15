package ru.pt.api.service.file;

import java.io.InputStream;

import ru.pt.api.dto.file.FileDownload;
import ru.pt.api.dto.file.FileModel;

import java.util.List;
import java.util.Map;

import ru.pt.domain.model.VariableContext;

/**
 * Работа с моделями файлов приложения
 * Задействовано файловое хранилище
 */
public interface FileService {

    /**
     * Загрузить содержимое файла
     * @param tid идентификатор тенанта
     * @param file массив байт с содержимым
     */
    Long uploadFile(String tenantCode, InputStream file, String filename, String contentType, Long size);

    /**
     * Скачать файл
     *
     * @param id идентификатор файла
     * @return содержимое, имя файла и content-type
     */
    FileDownload downloadFile(Long id);

    /**
     * Мягко удалить файл
     *
     * @param id идентификатор файла
     */
    void delete(Long id);

    /**
     * Обработать файл с параметрами
     *
     * @param id        идентификатор файла
     * @param keyValues параметры обработки
     * @return результат обработки
     */
    byte[] process(Long id, VariableContext keyValues);

    /**
     * Получить файл по типу и параметрам
     *
     * @param fileId  идентификатор файла
     * @param keyValues параметры поиска
     * @return файл в виде массива байт
     */
    byte[] getFile(Integer fileId, VariableContext keyValues);

}
