package ru.pt.process.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.process.Cover;
import ru.pt.api.dto.process.CoverInfo;
import ru.pt.api.dto.process.Deductible;
import ru.pt.api.dto.process.InsuredObject;
import ru.pt.api.dto.process.PolicyDTO;
import ru.pt.api.dto.product.*;
import ru.pt.api.service.process.PreProcessService;
import ru.pt.api.utils.JsonProjection;
import ru.pt.api.utils.JsonSetter;
import ru.pt.process.utils.PeriodUtils;
import ru.pt.process.utils.VariablesService;
import ru.pt.api.dto.product.PvVar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class PreProcessServiceImpl implements PreProcessService {

    public void normalizePolicyDates(PolicyDTO policy, ProductVersionModel productVersionModel) {
        
        if (policy.getIssueDate() == null) {
            policy.setIssueDate(ZonedDateTime.now());
        }
            setActivationDelay(policy, productVersionModel);
            setPolicyTerm(policy, productVersionModel);
    }

    @Override
    public void applyProductMetadata(PolicyDTO policy, ProductVersionModel productVersionModel) {
        
        normalizePolicyDates( policy,  productVersionModel);

        var insuredObject = policy.getInsuredObjects().get(0);

        if (insuredObject == null || insuredObject.getCovers() == null) {
            var emptyInsuredObject = new InsuredObject();
            emptyInsuredObject.setCovers(new ArrayList<>());
            insuredObject = emptyInsuredObject;
        }

        Integer inPackageNo;
        if (insuredObject.getPackageCode() == null) {
            inPackageNo = 0;
        } else {
            inPackageNo = insuredObject.getPackageCode();
        }
        final Integer pkgCode = inPackageNo;

        PvPackage pvPackage = productVersionModel.getPackages().stream()
                .filter(p -> p.getCode().equals(pkgCode))
                .findFirst()
                .orElse(null);

        if (pvPackage == null) {
            throw new IllegalArgumentException("Package not found: " + inPackageNo);
        }

        insuredObject.setPackageCode(pkgCode);

        List<PvCover> covers = pvPackage.getCovers();
        for (PvCover pvCover : covers) {
            // Check if the cover.code exists in policy.covers
            List<Cover> policyCovers = insuredObject.getCovers();
            boolean coverExists = false;
            if (policyCovers != null) {
                for (Cover policyCover : policyCovers) {
                    if (policyCover != null && policyCover.getCover() != null && pvCover.getCode().equals(policyCover.getCover().getCode())) {
                        coverExists = true;
                        break;
                    }
                }
            }
            if (!coverExists && pvCover.getIsMandatory()) {
                Cover newCover = new Cover();
                newCover.setCover(new CoverInfo(pvCover.getCode(), "", ""));
                coverExists = true;
                insuredObject.getCovers().add(newCover);
            }
            if (coverExists) {
                Cover policyCover = null;
                if (policyCovers != null) {
                    policyCover = policyCovers.stream()
                            .filter(c -> c.getCover() != null && c.getCover().getCode().equals(pvCover.getCode()))
                            .findFirst()
                            .orElse(null);
                }
                if (policyCover != null) {
                    String waitingPeriod = pvCover.getWaitingPeriod();
                    if (waitingPeriod != null && !waitingPeriod.isEmpty()) {
                        ZonedDateTime startDate = policy.getStartDate().plus(Period.parse(waitingPeriod));
                        policyCover.setStartDate(startDate);
                    } else {
                        policyCover.setStartDate(policy.getStartDate());
                    }
                    String coverageTerm = pvCover.getCoverageTerm();
                    if (coverageTerm != null && !coverageTerm.isEmpty()) {
                        ZonedDateTime endDate = policyCover.getStartDate().plus(Period.parse(coverageTerm));
                        policyCover.setEndDate(endDate);
                    } else {
                        policyCover.setEndDate(policy.getEndDate());
                    }


                    PvLimit pvLimit = getPvLimit(pvCover, policyCover.getSumInsured());
                    if (pvLimit != null) {
                        policyCover.setSumInsured(pvLimit.getSumInsured());
                        policyCover.setPremium(pvLimit.getPremium());
                    }

                    PvDeductible pvDeductible = getPvDeductible(pvCover, policyCover);
                    if (pvDeductible != null) {
                        policyCover.setDeductibleId(pvDeductible.getId());
                   } else {
                        policyCover.setDeductibleId(null);
                    }

                    policyCover.setCover(new CoverInfo(pvCover.getCode(), "", ""));
                }
            }
        }

        policy.setInsuredObjects(List.of(insuredObject));
    }

    public PolicyDTO setActivationDelay(PolicyDTO policy, ProductVersionModel policyVersionModel) {


        String validatorType = policyVersionModel.getWaitingPeriod().getValidatorType();
        String validatorValue = policyVersionModel.getWaitingPeriod().getValidatorValue();

        ZonedDateTime issueDate = policy.getIssueDate();
        ZonedDateTime startDate = policy.getStartDate();
        String waitingPeriod = policy.getWaitingPeriod();

        if (issueDate == null) {
            throw new IllegalAccessError("Issue date is required");
        }
        if (validatorType != null) {
            // TODO добавить даты в зависимости от указанного waitingPeriod
            switch (validatorType) {
                case "RANGE":
                    if (startDate == null) {
                        throw new IllegalAccessError("Start date is required");
                    }

                    if (!PeriodUtils.isDateInRange(issueDate, startDate, validatorValue)) {
                        throw new IllegalArgumentException("Activation delay is not in range");
                    }
                    Period prd = Period.between(issueDate.toLocalDate(), startDate.toLocalDate());
                    policy.setWaitingPeriod(prd.toString());
                    break;
                case "LIST":
                    // список доступных значений из модели полиса
                    // взять из договора policyTerm, проверить что это значение есть в списке. вычислить дату2
                    String[] list = validatorValue.split(",");
                    // если только одно значение, то только оно и возможно
                    if (list.length == 0) {
                        throw new IllegalAccessError("validatorValue is invalid");
                    } else if (list.length == 1) {
                        waitingPeriod = list[0];
                    } else {
                        if (waitingPeriod == null) {
                            throw new IllegalAccessError("Waiting period is required");
                        }

                        boolean found = false;
                        // check if policyTerm is in list array. loop through list and check if policyTerm is in list
                        for (String period : list) {
                            if (waitingPeriod.equals(period.trim())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            throw new IllegalAccessError("Waiting period is not in list");
                        }
                    }

                    startDate = issueDate.plus(Period.parse(waitingPeriod));

                    policy.setStartDate(startDate);
                    policy.setWaitingPeriod(waitingPeriod);
                    break;
                case "NEXT_MONTH":
                    startDate = issueDate.plus(Period.parse("P1M")).withDayOfMonth(1);
                    policy.setStartDate(startDate);
                    break;
            }
        }

        return policy;
    }

    public PolicyDTO setPolicyTerm(PolicyDTO policy, ProductVersionModel policyVersionModel) {
        // activationDelay - RANGE LIST NEXT_MONTH
        String validatorType = policyVersionModel.getPolicyTerm().getValidatorType();
        String validatorValue = policyVersionModel.getPolicyTerm().getValidatorValue();

        ZonedDateTime startDate = policy.getStartDate();
        ZonedDateTime endDate = policy.getEndDate();
        String policyTerm = policy.getPolicyTerm();

        if (startDate == null) {
            throw new BadRequestException("start date is required");
        }
        if (validatorType != null) {
            switch (validatorType) {
                case "RANGE":
                    if (endDate == null) {
                        throw new BadRequestException("End date is required");
                    }

                    if (!PeriodUtils.isDateInRange(startDate, endDate, validatorValue)) {
                        throw new IllegalArgumentException("Activation delay is not in range");
                    }
                    Period prd = Period.between(startDate.toLocalDate(), endDate.toLocalDate());
                    //setter.setRawValue("policyTerm", validatorValue);
                    policy.setPolicyTerm(prd.toString());
                    break;
                case "LIST":
                    // должно быть startDate и policyTerm в договоре и policyTerms в модели полиса
                    // список доступных значений из модели полиса
                    String[] list = validatorValue.split(",");
                    // если только одно значение, то только оно и возможно
                    if (list.length == 0) {
                        throw new IllegalAccessError("validatorValue is invalid");
                    } else if (list.length == 1) {
                        policyTerm = list[0];
                    } else {
                        if (policyTerm == null) {
                            throw new IllegalAccessError("Policy term is required");
                        }

                        boolean found = false;
                        // check if policyTerm is in list array. loop through list and check if policyTerm is in list
                        for (String period : list) {
                            if (policyTerm.equals(period.trim())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            throw new IllegalArgumentException("Policy term is not in list");
                        }
                    }

                    endDate = startDate.plus(Period.parse(policyTerm));

                    policy.setEndDate(endDate);
                    policy.setPolicyTerm(policyTerm);
                    break;
            }
        }

        return policy;
    }


    public static PvLimit getPvLimit(PvCover pvCover, BigDecimal sumInsured) {

        // если на покрытии только 1 лимит то он является единственно возможным
        // иначе проверяем, что переданная страховая сумма есть в списке возможных сумм
        if (pvCover.getLimits() != null && pvCover.getLimits().size() == 1) {
            return pvCover.getLimits().get(0);
        }

        if (sumInsured == null) {
            return null;
        }


        for (PvLimit pvLimit : pvCover.getLimits()) {
            if (Objects.equals(pvLimit.getSumInsured(), sumInsured)) {
                return pvLimit;
            }
        }
        return null;
    }

    public static PvDeductible getPvDeductible(PvCover pvCover, Cover policyCover) {
        // если франшиза обязательна и в списке только одно значение то берем его
        // если франшиза обязательна а щапросе не ередена ничего, то берем франшизу с минимальным номером
        // если чтото передано, то проверяем по списку что это значение есть
        Deductible deductible = policyCover.getDeductible();

        // В списке нет франшиз
        if (pvCover.getDeductibles() == null || pvCover.getDeductibles().isEmpty()) {
            return null;
        }

        if (deductible != null) {
        // Переданная франшиза есть в списке. Проверка по номеру из справочника.
        for (PvDeductible pvDed : pvCover.getDeductibles()) {
            if (deductible.getId().equals(pvDed.getId())) {
                return pvDed;
            }
        }
    }
        // Ничего не нашли по переданному. Проверяем что в покрытии есть обязательная франшиза.
        // тогда берем c минимальным номером
        if (pvCover.getIsDeductibleMandatory()) {
            List<PvDeductible> deductibles = pvCover.getDeductibles();
            if (deductibles != null && !deductibles.isEmpty()) {
                deductibles.sort(Comparator.comparingInt(d -> d.getId()));
                return deductibles.get(0);
            }
        }

        return null;
    }


}
