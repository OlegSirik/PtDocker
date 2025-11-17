package ru.pt.api.service.projection;

import java.util.Map;

/**
 * Получить данные по полису в формате json
 */
public interface ProjectionService {

    /**
     * Получить код продукта из json договора
     * @param json исходный договор
     * @return код продукта
     */
    String getProductCode(String json);

    /**
     * Извлечь параметры продукта из json договора
     * @param json исходный договор
     * @return карта параметров и значений
     */
    Map<String, String> getProductParams(String json);

}
