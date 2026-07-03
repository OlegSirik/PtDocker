package ru.pt.process.service;

import org.springframework.stereotype.Component;

import ru.pt.api.dto.policy.Cover;
import ru.pt.api.dto.policy.Deductible;
import ru.pt.api.dto.policy.InsuredObject;
import ru.pt.api.dto.product.PvVar;
import ru.pt.domain.model.VariableContext;

import java.math.BigDecimal;
import java.util.function.Function;

@Component
public class PostProcessService {

    public void setCovers(InsuredObject insuredObject, VariableContext ctx) {
        if (insuredObject == null || insuredObject.getCovers() == null) {
            return;
        }
        for (Cover cover : insuredObject.getCovers()) {
            if (cover == null || cover.getCover() == null) {
                continue;
            }
            String coverCode = cover.getCover().getCode();
            cover.setSumInsured(coverDecimal(ctx, coverCode, PvVar::varSumInsured));
            cover.setPremium(coverDecimal(ctx, coverCode, PvVar::varPremium));

            Long dedId = coverDeductibleNr(ctx, coverCode);
            if (dedId != null) {
                cover.setDeductible(new Deductible(dedId, null));
            }
            cover.setLimitMin(coverDecimal(ctx, coverCode, PvVar::varLimitMin));
            cover.setLimitMax(coverDecimal(ctx, coverCode, PvVar::varLimitMax));
        }
    }

    private static BigDecimal coverDecimal(
            VariableContext ctx,
            String coverCode,
            Function<String, PvVar> varFactory) {
        if (coverCode == null) {
            return null;
        }
        return ctx.getDecimal(varFactory.apply(coverCode).getVarCode());
    }

    private static Long coverDeductibleNr(VariableContext ctx, String coverCode) {
        if (coverCode == null) {
            return null;
        }
        String deductibleNr = ctx.getString(PvVar.varDeductibleNr(coverCode).getVarCode());
        if (deductibleNr == null || deductibleNr.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(deductibleNr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
