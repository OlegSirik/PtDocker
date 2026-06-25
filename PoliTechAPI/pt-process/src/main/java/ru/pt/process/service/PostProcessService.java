package ru.pt.process.service;

import org.springframework.stereotype.Component;

import ru.pt.api.dto.policy.Cover;
import ru.pt.api.dto.policy.Deductible;
import ru.pt.api.dto.policy.InsuredObject;
import ru.pt.domain.model.PolicyCoreView;
import ru.pt.domain.model.VariableContext;

@Component
public class PostProcessService {

    public void setCovers(InsuredObject insuredObject, VariableContext ctx) {
        PolicyCoreView policyView = new PolicyCoreView();

        if (insuredObject != null && insuredObject.getCovers() != null) {
            for (Cover cover : insuredObject.getCovers()) {
                if (cover == null || cover.getCover() == null) {
                    continue;
                }
                String coverCode = cover.getCover().getCode();
                cover.setSumInsured(policyView.getCoverSumInsured(ctx, coverCode));
                cover.setPremium(policyView.getCoverPremium(ctx, coverCode));

                Long dedId = policyView.getCoverDeductibleNr(ctx, coverCode);
                if (dedId != null) {
                    cover.setDeductible(new Deductible(dedId, null));
                }
                cover.setLimitMin(policyView.getCoverLimitMin(ctx, coverCode));
                cover.setLimitMax(policyView.getCoverLimitMax(ctx, coverCode));
            }
        }
    }
}
