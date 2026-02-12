package ru.pt.api.service.calculator;

import ru.pt.api.dto.calculator.CoefficientColumn;
import ru.pt.api.dto.calculator.CoefficientDataRow;
import ru.pt.domain.model.VariableContext;

import java.util.List;

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
    List<CoefficientDataRow> getTable(Integer calculatorId, String code);

    /**
     * Заменить таблицу коэффициента
     * @param calculatorId айди калькулятора
     * @param code код коэффициента
     * @param tableJson новая таблица коэффициентов
     * @return сохраненная таблица
     */
    List<CoefficientDataRow> replaceTable(Integer calculatorId, String code, List<CoefficientDataRow> tableJson);

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
                               VariableContext values,
                               List<CoefficientColumn> columns);

    /**
     * Копировать таблицу коэффициента между калькуляторами
     * @param calculatorIdFrom исходный калькулятор
     * @param calculatorIdTo целевой калькулятор
     * @param coefficientCode код коэффициента
     * @return количество скопированных строк
     */
    int copyCoefficient(Integer calculatorIdFrom, Integer calculatorIdTo, String coefficientCode);

    /**
     * Получить SQL запрос для коэффициента с именами переменных вместо значений
     * @param calculatorId айди калькулятора
     * @param coefficientCode код коэффициента
     * @param columns описание колонок коэффициента
     * @return SQL запрос в виде строки с именами переменных, или null если параметры невалидны
     */
    String getSQL(Integer calculatorId,
                  String coefficientCode,
                  List<CoefficientColumn> columns);

}
