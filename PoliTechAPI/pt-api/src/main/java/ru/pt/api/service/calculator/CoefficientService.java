package ru.pt.api.service.calculator;

import com.fasterxml.jackson.databind.node.ArrayNode;
import ru.pt.api.dto.calculator.CoefficientColumn;

import java.util.List;
import java.util.Map;

/**
 * Получение и редактирование коэффициентов
 */
public interface CoefficientService {

    /**
     * Получить таблицу коэффициента
     * @param calculatorId айди калькулятора
     * @param code код коэффициента
     * @return json представление таблицы коэффициентов
     */
    ArrayNode getTable(Integer calculatorId, String code);

    /**
     * Заменить таблицу коэффициента
     * @param calculatorId айди калькулятора
     * @param code код коэффициента
     * @param tableJson новая таблица коэффициентов
     * @return сохраненная таблица
     */
    ArrayNode replaceTable(Integer calculatorId, String code, ArrayNode tableJson);

    /**
     * Получить значение коэффициента по заданным параметрам
     * @param calculatorId айди калькулятора
     * @param coefficientCode код коэффициента
     * @param values значения переменных для поиска
     * @param columns описание колонок коэффициента
     * @return значение коэффициента в виде строки
     */
    String getCoefficientValue(Integer calculatorId,
                               String coefficientCode,
                               Map<String, String> values,
                               List<CoefficientColumn> columns);

}
