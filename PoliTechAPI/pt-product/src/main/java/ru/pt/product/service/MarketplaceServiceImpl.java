package ru.pt.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.marketplace.*;
import ru.pt.api.dto.product.*;
import ru.pt.api.service.marketplace.MarketplaceService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;
import ru.pt.api.service.auth.AccountProductRoles;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.product.entity.AttributeDefEntity;
import ru.pt.product.entity.ContractModelEntity;
import ru.pt.product.repository.AttributeDefRepository;
import ru.pt.product.repository.ContractModelRepository;
import ru.pt.api.service.db.ReferenceDataService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of marketplace integration service.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarketplaceServiceImpl implements MarketplaceService {

    private static final String MODEL_CODE = "box1";

    private final AccountProductRoles accountProductRoles;
    private final ProductService productService;
    private final LobService lobService;
    private final SecurityContextHelper securityContextHelper;
    private final ContractModelRepository contractModelRepository;
    private final AttributeDefRepository attributeDefRepository;
    private final ReferenceDataService referenceDataService;

    @Override
    public MpOwnerResponse getPageOwner() {
        // TODO: Replace with data from database
        MpOwnerResponse response = new MpOwnerResponse();
        MpLegalData legalData = new MpLegalData();
        legalData.setFullName("САО \"ВСК\"");
        legalData.setAddress("121552, Российская Федерация, г. Москва, ул. Островная, д.4");
        legalData.setInn("7710026574");
        legalData.setLicenseInfo("Лицензии СЛ 0621, СИ 0621, выданны ЦБ РФ без ограничения срока действия");
        legalData.setOgrn("1027700186062");
        legalData.setPhone("+74957852726");
        legalData.setEmail("info@vsk.ru");
        legalData.setPhoneCs("8-800-775-15-75");
        legalData.setWebSite("https://www.vsk.ru");
        response.setLegalData(legalData);
        response.setLogo("https://www.vsk.ru/cms/assets/dbc7b893-539e-4725-a81b-a97d9acbca77?");
        response.setTitle("ВСК");
        return response;
    }

    @Override
    public List<MpProductDto> getAllProducts() {
        Long accountId = getCurrentAccountId();

        List<ru.pt.api.dto.auth.ProductRole> roles = accountProductRoles.getProductRolesByAccountIdRaw(accountId);
        List<ru.pt.api.dto.auth.ProductRole> allowedRoles = roles.stream()
                .filter(r -> Boolean.TRUE.equals(r.canQuote()) && Boolean.TRUE.equals(r.canPolicy()))
                .toList();

        List<MpProductDto> result = new ArrayList<>();
        for (ru.pt.api.dto.auth.ProductRole role : allowedRoles) {
            Long productId = role.roleProductId();
            try {
                ProductVersionModel pv = productService.getProduct(productId.intValue(), false);
                if (pv == null) {
                    continue;
                }

                LobModel lob = lobService.getByCode(pv.getLob());
                Map<String, String> coverNames = getCoverNames(lob);

                if (pv.getPackages() == null) continue;

                for (PvPackage pkg : pv.getPackages()) {
                    MpProductDto dto = buildProductDto(pv, pkg, productId, coverNames);
                    if (dto != null) {
                        result.add(dto);
                    }
                }
            } catch (Exception e) {
                // Skip products that fail (e.g. no prod version)
                continue;
            }
        }
        return result;
    }

    @Override
    public FormMetadata getProduct(Integer productId) {
        Long tenantId = getCurrentTenantId();

        // Verify product exists and account has access
        ProductVersionModel pv = productService.getProduct(productId, false);
        if (pv == null) {
            throw new NotFoundException("Product not found: " + productId.toString());
        }

        FormMetadata metadata = new FormMetadata();

        ContractModelEntity model = contractModelRepository.findByTidAndCode(tenantId, MODEL_CODE)
                .orElseThrow(() -> new NotFoundException("Contract model not found: " + MODEL_CODE));
        metadata.setTitle(model.getName());

        metadata.setSections(List.of());
        metadata.setEntities(List.of());

        List<AttributeDefEntity> attributes = attributeDefRepository.findByTenantAndModelCode(tenantId, MODEL_CODE);
        Map<String, AttributeDefEntity> attrByVarCdm = attributes.stream()
                .collect(Collectors.toMap(AttributeDefEntity::getVarCdm, a -> a, (a, b) -> a));

        List<FormField> fields = new ArrayList<>();
        if (pv.getVars() != null) {
            for (PvVar var : pv.getVars()) {
                String varCdm = var.getVarCdm();
                if (varCdm == null) continue;
                AttributeDefEntity attr = attrByVarCdm.get(varCdm);
                if (attr != null) {
                    String type = mapVarTypeToFormType(attr.getVarDataType());
                    String code = attr.getVarCdm();
                    String key = var.getVarCode();

                    /* Если страхователь = равен объекту страхования то объект не добавляем */
                    if (pv.getRules() != null && pv.getRules().isInsuredEqualsPolicyHolder() && attr.getVarCdm().startsWith("insuredObject")) {
                        continue;
                    }
                    
                    if ( key.indexOf("Date") > 0 ) { type = "date"; }
                    FormField formField = new FormField( code, key, type, var.getVarName(), var.getVarName() );

                    Map<String, String> refData = referenceDataService.getRefData(code);
                        // For String type: check saveValidator then quoteValidator for IN_LIST rule with keyLeft = var.getVarCode()
                    ValidatorRule inListRule = findInListRule(pv, var.getVarCode());
                    if (inListRule != null) {
                        List<String> filterValues = parseFilterValues( inListRule.getValueRight() );
                            
                        if (!(filterValues == null || filterValues.isEmpty())) {
                            refData = referenceDataService.getRefData(code, filterValues);
                        }
                    }

                    if (refData != null && !refData.isEmpty()) {
                        List<FormFieldOption> options = refData.entrySet().stream()
                                .map(e -> {
                                    FormFieldOption opt = new FormFieldOption();
                                    opt.setValue(e.getKey());
                                    opt.setLabel(e.getValue());
                                    return opt;
                                })
                                .toList();
                        formField.setOptions(options);
                        formField.setType("select");
                    }
            
                    fields.add( formField );
                    
                }
            }
        }
        metadata.setFields(fields);

        

        return metadata;
    }

    private MpProductDto buildProductDto(ProductVersionModel pv, PvPackage pkg,
                                        Long productId, Map<String, String> coverNames) {
        PeriodRule policyTerm = pv.getPolicyTerm();
        String term = null;
        if (policyTerm != null && "LIST".equals(policyTerm.getValidatorType()) && policyTerm.getValidatorValue() != null) {
            String[] parts = policyTerm.getValidatorValue().split(",");
            if (parts.length == 1) {
                term = parts[0].trim();
            } else {
                return null;
            }
        }

        if (pkg.getCovers() == null || pkg.getCovers().isEmpty()) return null;

        List<MpCoverDto> covers = new ArrayList<>();
        BigDecimal totalLimit = BigDecimal.ZERO;
        BigDecimal totalPremium = BigDecimal.ZERO;

        for (PvCover cover : pkg.getCovers()) {
            List<PvLimit> limits = cover.getLimits();
            if (limits == null || limits.size() != 1) return null;

            PvLimit limit = limits.get(0);
            MpCoverDto dto = new MpCoverDto();
            dto.setCode(cover.getCode());
            dto.setName(coverNames.getOrDefault(cover.getCode(), cover.getCode()));
            dto.setLimit(limit.getSumInsured());
            dto.setPremium(limit.getPremium());
            covers.add(dto);

            if (limit.getSumInsured() != null) totalLimit = totalLimit.add(limit.getSumInsured());
            if (limit.getPremium() != null) totalPremium = totalPremium.add(limit.getPremium());
        }

        MpProductDto result = new MpProductDto();
        result.setProductId(productId);
        result.setProductCode(pv.getCode());
        result.setProductName(pv.getName());
        result.setPackageCode(String.valueOf(pkg.getCode()));
        result.setPackageName(pkg.getName());
        result.setTerm(term);
        result.setCovers(covers);
        result.setLimit(totalLimit);
        result.setPremium(totalPremium);
        return result;
    }

    private Map<String, String> getCoverNames(LobModel lob) {
        Map<String, String> map = new HashMap<>();
        if (lob != null && lob.getMpCovers() != null) {
            for (LobCover c : lob.getMpCovers()) {
                if (c.getCoverCode() != null) {
                    map.put(c.getCoverCode(), c.getCoverName() != null ? c.getCoverName() : c.getCoverCode());
                }
            }
        }
        return map;
    }

    private ValidatorRule findInListRule(ProductVersionModel pv, String varCode) {
        if (pv.getSaveValidator() != null) {
            ValidatorRule found = pv.getSaveValidator().stream()
                    .filter(r -> varCode.equals(r.getKeyLeft()) && "IN_LIST".equals(r.getRuleType()))
                    .findFirst()
                    .orElse(null);
            if (found != null) return found;
        }
        if (pv.getQuoteValidator() != null) {
            return pv.getQuoteValidator().stream()
                    .filter(r -> varCode.equals(r.getKeyLeft()) && "IN_LIST".equals(r.getRuleType()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private List<String> parseFilterValues(String valueRight) {
        if (valueRight == null || valueRight.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(valueRight.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String mapVarTypeToFormType(String varDataType) {
        if (varDataType == null) return "text";
        return switch (varDataType.toUpperCase()) {
            case "NUMBER" -> "number";
            case "DATE" -> "date";
            case "BOOLEAN" -> "checkbox";
            default -> "text";
        };
    }

    private Long getCurrentAccountId() {
        return securityContextHelper.getAuthenticatedUser()
                .map(u -> u.getActingAccountId() != null ? u.getActingAccountId() : u.getAccountId())
                .orElseThrow(() -> new ForbiddenException("User not authenticated"));
    }

    private Long getCurrentTenantId() {
        return securityContextHelper.getAuthenticatedUser()
                .map(u -> u.getTenantId())
                .orElseThrow(() -> new ForbiddenException("User not authenticated"));
    }
}
