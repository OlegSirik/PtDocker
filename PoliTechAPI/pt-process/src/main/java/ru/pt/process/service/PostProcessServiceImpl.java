package ru.pt.process.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.process.Cover;
import ru.pt.api.dto.process.InsuredObject;
import ru.pt.api.dto.process.PolicyDTO;
import ru.pt.api.service.process.PostProcessService;
import ru.pt.domain.model.VariableContext;

@Component
public class PostProcessServiceImpl implements PostProcessService {
    

    @Override
    public void setCovers(PolicyDTO policyDTO, VariableContext ctx) {
        for (InsuredObject insuredObject : policyDTO.getInsuredObjects()) {
            
            if (insuredObject != null && insuredObject.getCovers() != null) {
                for (Cover cover : insuredObject.getCovers()) {
                    if (cover == null || cover.getCover() == null) {
                        continue;
                    }
                    String coverCode = cover.getCover().getCode();
                    cover.setSumInsured(ctx.getCoverSumInsured(coverCode));
                    cover.setPremium(ctx.getCoverPremium(coverCode));
                    cover.setDeductibleId(ctx.getCoverDeductibleNr(coverCode));
                    cover.setLimitMin(ctx.getCoverLimitMin(coverCode));
                    cover.setLimitMax(ctx.getCoverLimitMax(coverCode));
                }
            }
        }
    }

}
