package ru.pt.api.service.numbers;

import ru.pt.api.dto.numbers.NumberGeneratorDescription;

import ru.pt.domain.model.VariableContext;

/**
 * Манипуляция номерами полисов и создание новых нумераторов
 */
public interface NumberGeneratorService {

    /**
     * Получить следующий номер
     * @param tid идентификатор Tenant
     * @param numberGeneratorDescription параметры для нумератора
     * @param values описание договора
     * @return номер полиса(уникальный)
     */
    String getNextNumber(Long tid, NumberGeneratorDescription numberGeneratorDescription, VariableContext values);

    /**
     * Создать новый алгоритм нумератора
     * @param tid идентификатор Tenant
     * @param numberGeneratorDescription параметры для создания
     */
    void create(Long tid, NumberGeneratorDescription numberGeneratorDescription);

    /**
     * Сбросить нумератор по id
     * @param tid идентификатор Tenant
     * @param id id нумератора
     */
    void reset(Long tid, Long id);




}
