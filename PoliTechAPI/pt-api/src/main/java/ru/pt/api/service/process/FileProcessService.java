package ru.pt.api.service.process;

/**
 * Интерфейс для работы с файлами договора
 */
public interface FileProcessService {

    /**
     * Сгенерировать файл по pdf-шаблону(заполнить поля из договора)
     * @param policyNumber номер полиса
     * @param printFormType тип печатки
     * @return файл
     */
    byte[] generatePrintForm(String policyNumber, String printFormType);

}
