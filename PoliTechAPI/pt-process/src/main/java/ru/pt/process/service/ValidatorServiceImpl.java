package ru.pt.process.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.errors.ValidationError;
import ru.pt.api.dto.process.ValidatorType;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.ValidatorRule;
import ru.pt.api.service.process.ValidatorService;
import ru.pt.domain.model.VariableContext;
import ru.pt.process.utils.ValidatorImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// TODO разбить на классы
@Component
public class ValidatorServiceImpl implements ValidatorService {

    private static final Logger logger = LoggerFactory.getLogger(ValidatorServiceImpl.class);

    public ValidatorServiceImpl() {}

    @Override
    public List<ValidationError> validate(ValidatorType validatorType, ProductVersionModel productVersionModel, VariableContext varCtx) {
        logger.debug("Starting validation for type: {}", validatorType);
        
        var result = new ArrayList<ValidationError>();
        List<ValidatorRule> validatorRules = List.of();
        if (ValidatorType.QUOTE.equals(validatorType)) {
            validatorRules = productVersionModel.getQuoteValidator();
            if (validatorRules != null) {
                logger.debug("Using quote validators, count: {}", validatorRules.size());
            } else {
                logger.debug("Quote validators list is null");
            }
        } else if (ValidatorType.SAVE.equals(validatorType)) {
            validatorRules = productVersionModel.getSaveValidator();
            if (validatorRules != null) {
                logger.debug("Using save validators, count: {}", validatorRules.size());
            } else {
                logger.debug("Save validators list is null");
            }
        }
        
        if (validatorRules == null || validatorRules.isEmpty()) {
            logger.debug("No validation rules found for type: {}", validatorType);
            return result;
        }

        // sort validatorRules by lineNr
        validatorRules.sort(
                Comparator.comparingInt(v -> v.getLineNr() != null ? v.getLineNr() : 0)
        );

        logger.info("Processing {} validation rules for type: {}", validatorRules.size(), validatorType);
        
        boolean isValidAnd = true;
        int ruleIndex = 0;

        for (ValidatorRule validatorRule : validatorRules) {
            ruleIndex++;
            String ruleKey = String.format("%s[%d]", validatorRule.getKeyLeft(), ruleIndex);
            
            logger.debug("Validating rule {}: keyLeft={}, ruleType={}, keyRight={}, valueRight={}, lineNr={}", 
                    ruleIndex, 
                    validatorRule.getKeyLeft(),
                    validatorRule.getRuleType(),
                    validatorRule.getKeyRight(),"",
                    //validatorRule.getValueRight(),
                    validatorRule.getLineNr());

            boolean isValid = false;
            try {
                isValid = ValidatorImpl.validate(varCtx, validatorRule);
                logger.debug("Rule {} validation result: {}", ruleKey, isValid ? "PASSED" : "FAILED");
            } catch (Exception e) {
                logger.error("Error validating rule {}: {}", ruleKey, validatorRule.getKeyLeft(), e);
                isValid = false;
            }

            if (validatorRule.getErrorText().equals("AND")) {
                isValidAnd = isValidAnd && isValid;
                logger.debug("AND condition at rule {}: current AND result={}, rule result={}, combined={}", 
                        ruleIndex, isValidAnd, isValid, isValidAnd);
            } else {
                if (isValidAnd) {
                    if (!isValid) {
                        String errorMessage = String.format("%s %s %s %s", 
                                validatorRule.getKeyLeft(),
                                validatorRule.getKeyRight(),"",
                                //validatorRule.getValueRight(),
                                validatorRule.getRuleType());
                        
                        ValidationError error = new ValidationError(
                                errorMessage,
                                validatorRule.getErrorText(),
                                "validation"
                        );
                        result.add(error);
                        
                        logger.warn("Validation failed for rule {}: {} - {}", 
                                ruleIndex, 
                                validatorRule.getKeyLeft(),
                                validatorRule.getErrorText());
                    } else {
                        logger.debug("Validation passed for rule {}: {}", ruleIndex, validatorRule.getKeyLeft());
                    }
                } else {
                    logger.debug("Skipping rule {} due to failed AND condition", ruleIndex);
                }
                isValidAnd = true;
            }
        }

        if (result.isEmpty()) {
            logger.info("All {} validation rules passed for type: {}", validatorRules.size(), validatorType);
        } else {
            logger.warn("Validation completed with {} errors out of {} rules for type: {}", 
                    result.size(), validatorRules.size(), validatorType);
        }

        return result;
    }

}
