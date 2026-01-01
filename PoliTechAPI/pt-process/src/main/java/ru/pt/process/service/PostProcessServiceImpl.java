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
                    cover.setSumInsured( ctx.getCoverSumInsured(cover.getCover().getCode()));
                    cover.setPremium( ctx.getCoverPremium(cover.getCover().getCode()));
                    cover.setDeductibleId( ctx.getCoverDeductibleNr(cover.getCover().getCode()));
                }
            }
        }
    }

}
