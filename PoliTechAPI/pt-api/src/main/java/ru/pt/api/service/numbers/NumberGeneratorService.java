package ru.pt.api.service.numbers;

import ru.pt.api.dto.errors.ValidationError;
import ru.pt.api.dto.numbers.NumberGeneratorDescription;

import java.util.List;

import ru.pt.domain.model.VariableContext;

/**
 * Манипуляция номерами полисов и создание новых нумераторов
 */
public interface NumberGeneratorService {

    /**
     * Получить следующий номер
     * @param values описание договора
     * @param productCode код продукта
     * @return номер полиса(уникальный)
     */
    String getNextNumber(VariableContext values, String productCode);

    /**
     * Создать новый алгоритм нумератора
     * @param numberGeneratorDescription параметры для создания
     */
    void create(NumberGeneratorDescription numberGeneratorDescription);

    /**
     * Обновить нумератор
     * @param numberGeneratorDescription параметры для обновления
     */
    void update(NumberGeneratorDescription numberGeneratorDescription);

    /**
     * Провалидировать поля для создания нумератора
     * @param numberGeneratorDescription описание
     * @return список ошибок
     */
    default List<ValidationError> validate(NumberGeneratorDescription numberGeneratorDescription){
        return List.of();
    }


}
