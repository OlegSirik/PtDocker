package ru.pt.process.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.process.Cover;
import ru.pt.api.dto.process.InsuredObject;
import ru.pt.api.dto.process.PolicyDTO;
import ru.pt.api.service.process.PostProcessService;
import ru.pt.api.service.projection.PolicyCoreViewInterface;
import ru.pt.domain.model.VariableContext;

@Component
public class PostProcessServiceImpl implements PostProcessService {
    
    
    @Override
    public void setCovers(PolicyDTO policyDTO, PolicyCoreViewInterface policyView) {
        
        for (InsuredObject insuredObject : policyDTO.getInsuredObjects()) {
            
            if (insuredObject != null && insuredObject.getCovers() != null) {
                for (Cover cover : insuredObject.getCovers()) {
                    if (cover == null || cover.getCover() == null) {
                        continue;
                    }
                    String coverCode = cover.getCover().getCode();
                    cover.setSumInsured(policyView.getCoverSumInsured(coverCode));
                    cover.setPremium(policyView.getCoverPremium(coverCode));
                    cover.setDeductibleId(policyView.getCoverDeductibleNr(coverCode));
                    cover.setLimitMin(policyView.getCoverLimitMin(coverCode));
                    cover.setLimitMax(policyView.getCoverLimitMax(coverCode));
                }
            }
        }
    }

}
