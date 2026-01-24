package ru.pt.process.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.api.dto.process.Cover;
import ru.pt.api.dto.process.CoverInfo;
import ru.pt.api.dto.process.Deductible;
import ru.pt.api.dto.process.InsuredObject;
import ru.pt.api.dto.process.PolicyDTO;
import ru.pt.api.dto.product.*;
import ru.pt.api.service.process.PreProcessService;
import ru.pt.process.utils.PeriodUtils;
import ru.pt.domain.model.ComputedVars;
import ru.pt.domain.model.VariableContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.time.ZoneId;

@Component
public class PreProcessServiceImpl implements PreProcessService {

    private static final Logger logger = LoggerFactory.getLogger(PreProcessServiceImpl.class);

    public void normalizePolicyDates(PolicyDTO policy, ProductVersionModel productVersionModel) {
        logger.debug("Normalizing policy dates. productCode={}", productVersionModel.getCode());
        
        if (policy.getIssueDate() == null) {
            policy.setIssueDate(ZonedDateTime.now());
            logger.debug("Set issue date to now: {}", policy.getIssueDate());
        }
            setActivationDelay(policy, productVersionModel);
            setPolicyTerm(policy, productVersionModel);
        logger.debug("Policy dates normalized. startDate={}, endDate={}", policy.getStartDate(), policy.getEndDate());
    }

    @Override
    public void applyProductMetadata(PolicyDTO policy, ProductVersionModel productVersionModel) {
        logger.info("Applying product metadata. productCode={}", productVersionModel.getCode());
        
        policy.setProductVersion(productVersionModel.getVersionNo());

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
            logger.warn("Package not found: packageCode={}", inPackageNo);
            throw new NotFoundException("Package not found: " + inPackageNo);
        }

        logger.debug("Resolved package: code={}, name={}", pvPackage.getCode(), pvPackage.getName());
        insuredObject.setPackageCode(pkgCode);

        List<PvCover> covers = pvPackage.getCovers();
        logger.debug("Processing {} covers for package {}", covers.size(), pkgCode);
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
        logger.debug("Setting activation delay. validatorType={}", policyVersionModel.getWaitingPeriod().getValidatorType());

        String validatorType = policyVersionModel.getWaitingPeriod().getValidatorType();
        String validatorValue = policyVersionModel.getWaitingPeriod().getValidatorValue();

        ZonedDateTime issueDate = policy.getIssueDate();
        ZoneId issueZone = issueDate != null ? issueDate.getZone() : ZoneId.systemDefault();
        logger.debug("Issue date is {}, timeZone is {}", issueDate, issueZone);

        ZonedDateTime startDate = policy.getStartDate();
        String waitingPeriod = policy.getWaitingPeriod();

        if (issueDate == null) {
            logger.warn("Issue date is required but not provided");
            throw new BadRequestException("Issue date is required");
        }
        if (validatorType != null) {
            // TODO добавить даты в зависимости от указанного waitingPeriod
            switch (validatorType) {
                case "RANGE": // Дата startDate должна попадать в период из настроек
                    if (startDate == null) {
                        throw new BadRequestException("Start date is required");
                    }

                    // Конвертируем startDate в эту временную зону
                    startDate = startDate.withZoneSameInstant(issueZone);
                    
                    if (!PeriodUtils.isDateInRange(issueDate, startDate, validatorValue)) {
                        throw new BadRequestException("Activation delay is not in range");
                    }
                    Period prd = Period.between(issueDate.toLocalDate(), startDate.toLocalDate());
                    policy.setWaitingPeriod(prd.toString());
                    break;
                case "LIST":
                    // список доступных значений из модели полиса
                    // взять из договора policyTerm, проdерить что это значение есть в списке. вычислить дату2
                    String[] list = validatorValue.split(",");
                    // если только одно значение, то только оно и возможно
                    if (list.length == 0) {
                        throw new UnprocessableEntityException("Ошибка настройки продукта. ActivalionDelay validatorValue is invalid");
                    } else if (list.length == 1) {
                        waitingPeriod = list[0];
                    } else {
                        if (waitingPeriod == null) {
                            throw new BadRequestException("Waiting period is required");
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
                            throw new BadRequestException("Waiting period is not in list");
                        }
                    }

                    startDate = issueDate.plus(Period.parse(waitingPeriod));

                    policy.setStartDate(startDate);
                    policy.setWaitingPeriod(waitingPeriod);
                    break;
                case "NEXT_MONTH":
                    startDate = issueDate.plus(Period.parse("P1M")).withDayOfMonth(1);
                    policy.setStartDate(startDate);
                    logger.debug("Set start date to next month: {}", startDate);
                    break;
            }
        } else {
            throw new UnprocessableEntityException("Не заданана настройка для activationPeriod");
        }

        // Проверить что startDate >= issueDate, привести в одну таймзону, если startDate не сегодня, то установить время 00:00:00
        startDate = policy.getStartDate();
        if (startDate != null && issueDate != null) {
            startDate = startDate.withZoneSameInstant(issueZone);
            
            if (startDate.isBefore(issueDate)) {
                logger.warn("Start date must be >= issue date. issueDate={}, startDate={}", issueDate, startDate);
                throw new BadRequestException("Start date must be >= issue date");
            }
            LocalDate todayInZone = ZonedDateTime.now(issueZone).toLocalDate();
            if (!startDate.toLocalDate().equals(todayInZone)) {
                startDate = startDate.toLocalDate().atStartOfDay(issueZone);
                logger.debug("Normalized startDate to 00:00:00 (not today). startDate={}", startDate);
            }
            policy.setStartDate(startDate);
        }

        logger.debug("Activation delay set. waitingPeriod={}, startDate={}", policy.getWaitingPeriod(), policy.getStartDate());
        return policy;
    }

    
    public PolicyDTO setPolicyTerm(PolicyDTO policy, ProductVersionModel policyVersionModel) {
        logger.debug("Setting policy term. validatorType={}", policyVersionModel.getPolicyTerm().getValidatorType());
        // activationDelay - RANGE LIST NEXT_MONTH
        String validatorType = policyVersionModel.getPolicyTerm().getValidatorType();
        String validatorValue = policyVersionModel.getPolicyTerm().getValidatorValue();

        ZonedDateTime issueDate = policy.getIssueDate();
        ZoneId issueZone = issueDate != null ? issueDate.getZone() : ZoneId.systemDefault();

        ZonedDateTime startDate = policy.getStartDate();
        ZonedDateTime endDate = policy.getEndDate();
        String policyTerm = policy.getPolicyTerm();

        if (startDate == null) {
            logger.warn("Start date is required but not provided");
            throw new BadRequestException("start date is required");
        }
        if (validatorType != null) {
            switch (validatorType) {
                case "RANGE":
                    if (endDate == null) {
                        throw new BadRequestException("End date is required");
                    }

                    // Конвертируем endDate в эту временную зону
                    endDate = endDate.withZoneSameInstant(issueZone);

                    if (!PeriodUtils.isDateInRange(startDate, endDate, validatorValue)) {
                        throw new BadRequestException("Activation delay is not in range");
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
                        throw new BadRequestException("validatorValue is invalid");
                    } else if (list.length == 1) {
                        policyTerm = list[0];
                    } else {
                        if (policyTerm == null) {
                            throw new BadRequestException("Policy term is required");
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
                            throw new BadRequestException("Policy term is not in list");
                        }
                    }

                    endDate = startDate.plus(Period.parse(policyTerm));

                    policy.setEndDate(endDate);
                    policy.setPolicyTerm(policyTerm);
                    logger.debug("Set end date from policy term. policyTerm={}, endDate={}", policyTerm, endDate);
                    break;
            }
        } else {
            throw new UnprocessableEntityException("Не заданана настройка для policyTerm");
        }

        endDate = policy.getEndDate();
        startDate = policy.getStartDate();
        if (endDate != null && startDate != null) {
            endDate = endDate.withZoneSameInstant(issueZone);
            endDate = endDate.toLocalDate().atStartOfDay(issueZone).minusSeconds(1);
            if (!endDate.isAfter(startDate)) {
                logger.warn("End date must be after start date. startDate={}, endDate={}", startDate, endDate);
                throw new BadRequestException("End date must be after start date");
            }
            policy.setEndDate(endDate);
            logger.debug("Normalized endDate to 00:00:00 minus 1 second. endDate={}", endDate);
        }

        logger.debug("Policy term set. policyTerm={}, endDate={}", policy.getPolicyTerm(), policy.getEndDate());
        return policy;
    }


    public static PvLimit getPvLimit(PvCover pvCover, BigDecimal sumInsured) {
        logger.debug("Resolving limit. coverCode={}, requestedSumInsured={}", pvCover.getCode(), sumInsured);

        // если на покрытии только 1 лимит то он является единственно возможным
        // иначе проверяем, что переданная страховая сумма есть в списке возможных сумм
        if (pvCover.getLimits() != null && pvCover.getLimits().size() == 1) {
            logger.debug("Single limit found for cover {}", pvCover.getCode());
            return pvCover.getLimits().get(0);
        }

        if (sumInsured == null) {
            logger.debug("No sum insured provided, returning null");
            return null;
        }


        for (PvLimit pvLimit : pvCover.getLimits()) {
            if (Objects.equals(pvLimit.getSumInsured(), sumInsured)) {
                logger.debug("Matched limit. sumInsured={}, premium={}", pvLimit.getSumInsured(), pvLimit.getPremium());
                return pvLimit;
            }
        }
        logger.warn("No matching limit found for sumInsured={}", sumInsured);
        return null;
    }

    public static PvDeductible getPvDeductible(PvCover pvCover, Cover policyCover) {
        logger.debug("Resolving deductible. coverCode={}, isMandatory={}", pvCover.getCode(), pvCover.getIsDeductibleMandatory());
        // если франшиза обязательна и в списке только одно значение то берем его
        // если франшиза обязательна а щапросе не ередена ничего, то берем франшизу с минимальным номером
        // если чтото передано, то проверяем по списку что это значение есть
        Deductible deductible = policyCover.getDeductible();

        // В списке нет франшиз
        if (pvCover.getDeductibles() == null || pvCover.getDeductibles().isEmpty()) {
            logger.debug("No deductibles available for cover {}", pvCover.getCode());
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

    public void enrichVariables(VariableContext ctx) {
        ComputedVars.getMagicValue(ctx, "ph_age_issue");
        ComputedVars.getMagicValue(ctx, "ph_age_end" );
        ComputedVars.getMagicValue(ctx, "io_age_issue" );
        ComputedVars.getMagicValue(ctx, "io_age_end" );
        ComputedVars.getMagicValue(ctx, "pl_TermMonths" );
        ComputedVars.getMagicValue(ctx, "pl_TermDays" );

    }
}
