package ru.pt.process.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.errors.ValidationError;
import ru.pt.api.dto.process.ValidatorType;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.ValidatorRule;
import ru.pt.api.service.process.PreProcessService;
import ru.pt.api.service.process.ValidatorService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;
import ru.pt.api.utils.JsonProjection;
import ru.pt.domain.model.VariableContext;
import ru.pt.process.utils.ValidatorImpl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO разбить на классы
@Component
public class ValidatorServiceImpl implements ValidatorService {


    public ValidatorServiceImpl() {}

    @Override
    public List<ValidationError> validate(ValidatorType validatorType, ProductVersionModel productVersionModel, VariableContext varCtx) {
        var result = new ArrayList<ValidationError>();
        var validatorRules = List.<ValidatorRule>of();
        if (ValidatorType.QUOTE.equals(validatorType)) {
            validatorRules = productVersionModel.getQuoteValidator();
        } else if (ValidatorType.SAVE.equals(validatorType)) {
            validatorRules = productVersionModel.getSaveValidator();
        }
        if (!validatorRules.isEmpty()) {
            // sort validatorRules by lineNr
            validatorRules.sort(
                    Comparator.comparingInt(v -> v.getLineNr() != null ? v.getLineNr() : 0)
            );

            boolean isValidAnd = true;

            for (ValidatorRule validatorRule : validatorRules) {
                boolean isValid = ValidatorImpl.validate(
                        varCtx,
                        validatorRule
                );

                if (validatorRule.getErrorText().equals("AND")) {
                    isValidAnd = isValidAnd && isValid;
                } else {
                    if (isValidAnd) {
                        if (!isValid) {
                            result.add(
                                    new ValidationError(
                                            validatorRule.getKeyLeft() + " " + validatorRule.getKeyRight() + " " + validatorRule.getValueRight() + " " + validatorRule.getRuleType(),
                                            validatorRule.getErrorText(),
                                            "validation"
                                    ));
                        }

                    }
                    isValidAnd = true;
                }
            }
        }

        return result;
    }

}
