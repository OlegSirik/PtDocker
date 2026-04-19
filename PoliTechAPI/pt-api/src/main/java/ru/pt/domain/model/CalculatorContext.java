package ru.pt.domain.model;

/**
 * Расширенный контекст переменных для калькулятора тарифов.
 * Наследует {@link VariableContext} и дополнительно позволяет хранить
 * произвольные вспомогательные значения и выполнять спец‑расчёт.
 */
public interface CalculatorContext extends VariableContext  {

    /**
     * Сохраняет произвольное значение в контекст по ключу.
     *
     * @param key   логический ключ значения
     * @param value значение, связанное с ключом
     * @return предыдущее значение по этому ключу или {@code null}, если его не было
     */
    Object put(String key, Object value);

}
