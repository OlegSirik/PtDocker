package ru.pt.api.service.process;

import ru.pt.api.dto.process.InsuredObject;
import ru.pt.api.dto.process.PolicyDTO;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.PvVar;

import java.util.List;

/**
 * Действия до выполнения операций с договором
 */
public interface PreProcessService {
    /**
     * Дозаполнить договор вычисляемыми значениями
     *
     * @param policy              договор
     * @param productVersionModel данные по продукту
     * @return дозаполненный договор
     */
    //String enrichPolicy(String policy, ProductVersionModel productVersionModel);

    /**
     * Вычислить значения переменных по jsonPath договора
     *
     * @param policy      договор
     * @param lobModel    данные о переменных Линии Бизнеса
     * @param productCode код продукта
     * @return список переменных и значений
     */
    //List<PvVar> evaluateAndEnrichVariables(String policy, List<PvVar> pvVars, String productCode);

    /**
     * Добавить переменные для сохранения результатов расчета
     *
     * @param insObject объект с рисками и покрытиями
     * @param lobVars   переменные от продукта
     */
    //void enrichVariablesBeforeCalculation(InsuredObject insObject, List<PvVar> pvVars);

    /**
     * Дозаполнить риски и покрытия в страхуемом объекте
     *
     * @param policy              договор
     * @param productVersionModel продукт
     * @return страхуемый объект с дозаполненными полями
     */
    //InsuredObject getInsuredObject(String policy, ProductVersionModel productVersionModel);


    /**
     * Дозаполнить договор вычисляемыми значениями
     *
     * @param policy              договор
     * @param productVersionModel данные по продукту
     * @return дозаполненный договор
     */
    //PolicyDTO enrichPolicy(PolicyDTO policy, ProductVersionModel productVersionModel);

    /**
     * Вычислить значения переменных по jsonPath договора
     *
     * @param policy      договор
     * @param lobModel    данные о переменных Линии Бизнеса
     * @param productCode код продукта
     * @return список переменных и значений
     */
    //List<PvVar> evaluateAndEnrichVariables(PolicyDTO policy, List<PvVar> pvVars, String productCode);


    /**
     * Дозаполнить риски и покрытия в страхуемом объекте
     * Меняет policy !!!
     *
     * @param policy              договор
     * @param productVersionModel продукт
     * @return страхуемый объект с дозаполненными полями
     */
    void applyProductMetadata(PolicyDTO policy, ProductVersionModel productVersionModel);

}
