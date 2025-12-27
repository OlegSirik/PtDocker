package ru.pt.process.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.process.Cover;
import ru.pt.api.dto.process.InsuredObject;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.service.process.PostProcessService;

import java.util.List;

@Component
public class PostProcessServiceImpl implements PostProcessService {
    @Override
    public InsuredObject setCovers(InsuredObject insuredObject, List<PvVar> calculatedValues) {
        if (insuredObject != null && insuredObject.getCovers() != null) {
            for (Cover cover : insuredObject.getCovers()) {
                if (cover == null || cover.getCover() == null) {
                    continue;
                }
                String sumInsuredVarCode = "co_" + cover.getCover().getCode() + "_sumInsured";
                String premiumVarCode = "co_" + cover.getCover().getCode() + "_premium";
                String deductibleNrVarCode = "co_" + cover.getCover().getCode() + "_deductibleNr";

                // TODO медленно, надо мапу передавать в метод
                // Find values in lobVars
                String sumInsured = calculatedValues.stream()
                        .filter(v -> sumInsuredVarCode.equals(v.getVarCode()))
                        .map(PvVar::getVarValue)
                        .findFirst()
                        .orElse(null);

                String premium = calculatedValues.stream()
                        .filter(v -> premiumVarCode.equals(v.getVarCode()))
                        .map(PvVar::getVarValue)
                        .findFirst()
                        .orElse("0.0");

                String deductibleNr = calculatedValues.stream()
                        .filter(v -> deductibleNrVarCode.equals(v.getVarCode()))
                        .map(PvVar::getVarValue)
                        .findFirst()
                        .orElse(null);

                // Set values to cover if setters exist
                if (sumInsured != null) {
                    try {
                        cover.setSumInsured(Double.parseDouble(sumInsured));
                    } catch (Exception ignored) {
                    }
                }
                try {
                    cover.setPremium(Double.parseDouble(premium));
                } catch (Exception ignored) {
                }
                if (deductibleNr != null) {
                    try {
                        cover.setDeductibleId(Integer.parseInt(deductibleNr));
                        
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return insuredObject;
    }
}
