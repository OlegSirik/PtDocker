package ru.pt.process.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.errors.ValidationError;
import ru.pt.api.dto.process.ValidatorType;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.LobVar;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.ValidatorRule;
import ru.pt.api.service.process.PreProcessService;
import ru.pt.api.service.process.ValidatorService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;
import ru.pt.api.utils.JsonProjection;
import ru.pt.process.utils.ValidatorImpl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO разбить на классы
@Component
public class ValidatorServiceImpl implements ValidatorService {

    private final ProductService productService;
    private final LobService lobService;
    private final PreProcessService preProcessService;

    public ValidatorServiceImpl(
            ProductService productService,
            LobService lobService,
            PreProcessService preProcessService
    ) {
        this.productService = productService;
        this.lobService = lobService;
        this.preProcessService = preProcessService;
    }

    @Override
    public List<ValidationError> validate(String policy, ValidatorType validatorType) {
        JsonProjection projection = new JsonProjection(policy);

        String productCode = projection.getProductCode();

        ProductVersionModel productVersionModel = productService.getProductByCode(productCode, true);

        LobModel lobModel = lobService.getByCode(productVersionModel.getLob());

        policy = preProcessService.enrichPolicy(policy, productVersionModel);
        // Fill Key-Value pairs for LOB Variables
        List<LobVar> lobVars = preProcessService.evaluateAndEnrichVariables(policy, lobModel, productCode);

        // TODO полис тоже надо возвращать(не забыть при рефакторе)
        return validate(validatorType, productVersionModel, lobVars);
    }


    @Override
    public List<ValidationError> validate(ValidatorType validatorType, ProductVersionModel productVersionModel, List<LobVar> lobVars) {
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

            Map<String, LobVar> context = lobVars.stream()
                    .collect(Collectors.toMap(LobVar::getVarCode, Function.identity()));

            for (ValidatorRule validatorRule : validatorRules) {
                boolean isValid = ValidatorImpl.validate(
                        context,
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


    public Map<String, Object> getMapVars(List<LobVar> lobVars) {
        Map<String, Object> mapVars = new HashMap<>();
        for (LobVar lobVar : lobVars) {
            mapVars.put(lobVar.getVarCode(), lobVar.getVarValue());
        }
        return mapVars;
    }

}
