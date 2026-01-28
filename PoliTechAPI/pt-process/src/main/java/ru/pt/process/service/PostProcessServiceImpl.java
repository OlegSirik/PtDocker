package ru.pt.process.service;

import org.springframework.stereotype.Component;

import ru.pt.api.dto.process.Cover;
import ru.pt.api.dto.process.InsuredObject;
import ru.pt.api.dto.process.PolicyDTO;
import ru.pt.api.service.process.PostProcessService;
import ru.pt.domain.model.PolicyCoreView;
import ru.pt.domain.model.VariableContext;

@Component
public class PostProcessServiceImpl implements PostProcessService {

    @Override
    public void setCovers(PolicyDTO policyDTO, VariableContext ctx) {
        // Create a projection view for this specific VariableContext
        PolicyCoreView policyView = new PolicyCoreView();

        for (InsuredObject insuredObject : policyDTO.getInsuredObjects()) {
            if (insuredObject != null && insuredObject.getCovers() != null) {
                for (Cover cover : insuredObject.getCovers()) {
                    if (cover == null || cover.getCover() == null) {
                        continue;
                    }
                    String coverCode = cover.getCover().getCode();
                    cover.setSumInsured(policyView.getCoverSumInsured(ctx, coverCode));
                    cover.setPremium(policyView.getCoverPremium(ctx, coverCode));
                    cover.setDeductibleId(policyView.getCoverDeductibleNr(ctx, coverCode));
                    cover.setLimitMin(policyView.getCoverLimitMin(ctx, coverCode));
                    cover.setLimitMax(policyView.getCoverLimitMax(ctx, coverCode));
                }
            }
        }
    }
}
