package ru.pt.api.service.calculator;

import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.calculator.CalculatorTemplateLine;
import ru.pt.api.dto.calculator.CalculatorTemplate;
import ru.pt.domain.model.CalculatorContext;
import java.util.List;

/**
 * Методы для CRUD операций с калькуляторами и для расчета страховой премии договора
 */
public interface CalculatorService {

    /**
     * Получить сохраненный калькулятор продукта
     * @param productId айди продукта
     * @param versionNo номер версии
     * @param packageNo номер пакета
     * @return модель калькулятора или null, если не найден
     */
    CalculatorModel getCalculator(Long tenantId, Long productId, Long versionNo, String packageNo);

    /**
     * Создать калькулятор, если он отсутствует
     * @param productId айди продукта
     * @param productCode код продукта
     * @param versionNo номер версии
     * @param packageNo номер пакета
     * @return созданный или найденный калькулятор
     */
    CalculatorModel createCalculator(Long tenantId, Long productId, Long versionNo, String packageNo, Long templateId);

    /**
     * копирует калькулятор в другой продукт версию
     * @param tenantId айди тенанта
     * @param productId айди продукта
     * @param versionNo номер версии
     * @param packageNo номер пакета
     * @return модель калькулятора
     */
    public void copyCalculator(Long tenantId, Long productId, Long versionNo, String packageNo, Long versionNoTo) ;

    /**
     * Выполнить калькулятор и вернуть рассчитанные переменные
     * @param tenantId айди тенанта
     * @param productId айди продукта
     * @param versionNo номер версии
     * @param packageNo номер пакета
     * @param inputValues входные переменные
     * @return список переменных с рассчитанными значениями
     */
    void runCalculator(Long tenantId, Long productId, Long versionNo, String packageNo, CalculatorContext ctx);

    /**
     * Заменить модель калькулятора
     * @param tenantId айди тенанта
     * @param productId айди продукта
     * @param productCode код продукта
     * @param versionNo номер версии
     * @param packageNo номер пакета
     * @param newJson новая модель калькулятора
     * @return сохраненная модель
     */
    CalculatorModel replaceCalculator(Long tenantId, Long productId, String productCode, Long versionNo,
                                      String packageNo, CalculatorModel newJson);

    /**
     * Синхронизировать переменные калькулятора с продуктом
     * @param calculatorId идентификатор калькулятора
     */
    void syncVars(Long tenantId, Long calculatorId);

    /**
     * Получить калькулятор по идентификатору
     * @param calculatorId идентификатор калькулятора
     * @return модель калькулятора или null, если не найден
     */
    CalculatorModel getCalculatorById(Long tenantId, Long calculatorId);

    void deleteCalculator(Long tenantId, Long productId, Long versionNo, String packageNo);

    /**
     * Создать шаблон формулы по существующему калькулятору для заданной линии бизнеса.
     */
    CalculatorTemplate createTemplate(Long tenantId, String lobCode, Long calculatorId);

    /**
     * Получить все шаблонные строки формул для линии бизнеса.
     */
    List<CalculatorTemplate> getTemplates(Long tenantId, String lobCode);

    /**
     * Создать шаблон формулы по существующему калькулятору для заданной линии бизнеса.
     */
    CalculatorTemplate updateTemplateName(Long tenantId, Long templateId, String templateName);

    /**
     * Удалить шаблон формулы
     * @param tenantId айди тенанта
     * @param templateId идентификатор шаблона
     */
    void deleteTemplate(Long tenantId, Long templateId);

}
