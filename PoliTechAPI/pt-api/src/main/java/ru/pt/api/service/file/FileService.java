package ru.pt.api.service.file;

import ru.pt.api.dto.file.FileModel;

import java.util.List;
import java.util.Map;

public interface FileService {

    /**
     * Создать метаданные файла
     * @param fileType тип файла
     * @param fileDesc описание файла
     * @param productCode код продукта
     * @param packageCode код пакета
     * @return модель файла
     */
    FileModel createMeta(String fileType, String fileDesc, String productCode, Integer packageCode);

    /**
     * Загрузить содержимое файла
     * @param id идентификатор файла
     * @param file массив байт с содержимым
     */
    void uploadBody(Long id, byte[] file);

    /**
     * Получить список файлов по продукту
     * @param productCode код продукта
     * @return список описаний файлов
     */
    List<Map<String, Object>> list(String productCode);

    /**
     * Скачать файл
     * @param id идентификатор файла
     * @return файл в виде массива байт
     */
    byte[] download(Long id);

    /**
     * Мягко удалить файл
     * @param id идентификатор файла
     */
    void softDelete(Long id);

    /**
     * Обработать файл с параметрами
     * @param id идентификатор файла
     * @param keyValues параметры обработки
     * @return результат обработки
     */
    byte[] process(Long id, Map<String, String> keyValues);

    /**
     * Получить файл по типу и параметрам
     * @param fileType тип файла
     * @param keyValues параметры поиска
     * @return файл в виде массива байт
     */
    byte[] getFile(String fileType, Map<String, String> keyValues);

}
