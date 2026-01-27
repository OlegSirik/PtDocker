package ru.pt.api.service.process;


import ru.pt.api.dto.process.PolicyDTO;
import ru.pt.api.service.projection.PolicyCoreViewInterface;


/**
 * Дополнительная логика после выполнения определенных процессов в моменте обработки договора
 */
public interface PostProcessService {

    /**
     * Заполнить премию по рискам
     *
     * @param insuredObject    страхуемый объект
     * @param calculatedValues переменные после расчета
     * @return заполненный объект
     */
    void setCovers(PolicyDTO policyDTO, PolicyCoreViewInterface policyView);

}
