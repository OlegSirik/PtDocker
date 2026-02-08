package ru.pt.api.service.calculator;

import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.product.PvVar;
import ru.pt.domain.model.VariableContext;

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
    CalculatorModel getCalculator(Integer productId, Integer versionNo, Integer packageNo);

    /**
     * Создать калькулятор, если он отсутствует
     * @param productId айди продукта
     * @param productCode код продукта
     * @param versionNo номер версии
     * @param packageNo номер пакета
     * @return созданный или найденный калькулятор
     */
    CalculatorModel createCalculatorIfMissing(Integer productId, String productCode, Integer versionNo, Integer packageNo);

    /**
     * копирует калькулятор в другой продукт версию
     * @param productId айди продукта
     * @param versionNo номер версии
     * @param packageNo номер пакета
     * @return модель калькулятора
     */
    public void copyCalculator(Integer productId, Integer versionNo, Integer packageNo, Integer versionNoTo) ;

    /**
     * Выполнить калькулятор и вернуть рассчитанные переменные
     * @param productId айди продукта
     * @param versionNo номер версии
     * @param packageNo номер пакета
     * @param inputValues входные переменные
     * @return список переменных с рассчитанными значениями
     */
    void runCalculator(Integer productId, Integer versionNo, Integer packageNo, VariableContext ctx);

    /**
     * Заменить модель калькулятора
     * @param productId айди продукта
     * @param productCode код продукта
     * @param versionNo номер версии
     * @param packageNo номер пакета
     * @param newJson новая модель калькулятора
     * @return сохраненная модель
     */
    CalculatorModel replaceCalculator(Integer productId, String productCode, Integer versionNo,
                                      Integer packageNo, CalculatorModel newJson);

    /**
     * Синхронизировать переменные калькулятора с продуктом
     * @param calculatorId идентификатор калькулятора
     */
    void syncVars(Integer calculatorId);

    void deleteCalculator(Integer productId, Integer versionNo, Integer packageNo);

}
