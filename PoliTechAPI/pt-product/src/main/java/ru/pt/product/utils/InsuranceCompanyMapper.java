package ru.pt.product.utils;

import ru.pt.api.dto.product.InsuranceCompanyDto;
import ru.pt.product.entity.InsuranceCompanyEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Поля id, tid, code, name, status — в колонках сущности; остальное — key-value в {@code other_props} (jsonb).
 */
public final class InsuranceCompanyMapper {

    public static final String K_SHORT_NAME = "shortName";
    public static final String K_FULL_NAME = "fullName";
    public static final String K_EGR = "egr";
    public static final String K_POSTAL_ADDRESS = "postal_address";
    public static final String K_LEGAL_ADDRESS = "legal_address";
    public static final String K_PHONE = "phone";
    public static final String K_MAIL = "mail";
    public static final String K_INN = "inn";
    public static final String K_KPP = "kpp";
    public static final String K_OKPO = "okpo";
    public static final String K_OGRN = "ogrn";
    public static final String K_ACCOUNT = "account";
    public static final String K_BANK = "bank";
    public static final String K_BIC = "bic";
    public static final String K_CORR_ACCOUNT = "corr_account";
    public static final String K_DISPLAY_NAME = "display_name";
    public static final String K_REPRESENTATIVE_STRING = "representative_string";

    private InsuranceCompanyMapper() {
    }

    public static InsuranceCompanyDto toDto(InsuranceCompanyEntity e) {
        if (e == null) {
            return null;
        }
        Map<String, String> p = e.getOtherProps() != null ? e.getOtherProps() : Map.of();
        return InsuranceCompanyDto.builder()
                .id(e.getId())
                .tid(e.getTid())
                .code(e.getCode())
                .name(e.getName())
                .status(e.getStatus())
                .shortName(p.get(K_SHORT_NAME))
                .fullName(p.get(K_FULL_NAME))
                .egr(p.get(K_EGR))
                .postalAddress(p.get(K_POSTAL_ADDRESS))
                .legalAddress(p.get(K_LEGAL_ADDRESS))
                .phone(p.get(K_PHONE))
                .mail(p.get(K_MAIL))
                .inn(p.get(K_INN))
                .kpp(p.get(K_KPP))
                .okpo(p.get(K_OKPO))
                .ogrn(p.get(K_OGRN))
                .account(p.get(K_ACCOUNT))
                .bank(p.get(K_BANK))
                .bic(p.get(K_BIC))
                .corrAccount(p.get(K_CORR_ACCOUNT))
                .displayName(p.get(K_DISPLAY_NAME))
                .representativeString(p.get(K_REPRESENTATIVE_STRING))
                .build();
    }

    /** code, name, other_props; status задаётся в сервисе ({@code normalizeStatus}). */
    public static void applyDtoToEntity(InsuranceCompanyDto dto, InsuranceCompanyEntity e) {
        e.setCode(dto.getCode());
        e.setName(dto.getName());
        e.setOtherProps(buildOtherPropsFromDto(dto));
    }

    public static Map<String, String> buildOtherPropsFromDto(InsuranceCompanyDto dto) {
        Map<String, String> m = new LinkedHashMap<>();
        putIfNotBlank(m, K_SHORT_NAME, dto.getShortName());
        putIfNotBlank(m, K_FULL_NAME, dto.getFullName());
        putIfNotBlank(m, K_EGR, dto.getEgr());
        putIfNotBlank(m, K_POSTAL_ADDRESS, dto.getPostalAddress());
        putIfNotBlank(m, K_LEGAL_ADDRESS, dto.getLegalAddress());
        putIfNotBlank(m, K_PHONE, dto.getPhone());
        putIfNotBlank(m, K_MAIL, dto.getMail());
        putIfNotBlank(m, K_INN, dto.getInn());
        putIfNotBlank(m, K_KPP, dto.getKpp());
        putIfNotBlank(m, K_OKPO, dto.getOkpo());
        putIfNotBlank(m, K_OGRN, dto.getOgrn());
        putIfNotBlank(m, K_ACCOUNT, dto.getAccount());
        putIfNotBlank(m, K_BANK, dto.getBank());
        putIfNotBlank(m, K_BIC, dto.getBic());
        putIfNotBlank(m, K_CORR_ACCOUNT, dto.getCorrAccount());
        putIfNotBlank(m, K_DISPLAY_NAME, dto.getDisplayName());
        putIfNotBlank(m, K_REPRESENTATIVE_STRING, dto.getRepresentativeString());
        return m;
    }

    private static void putIfNotBlank(Map<String, String> m, String key, String value) {
        if (value != null && !value.isBlank()) {
            m.put(key, value.trim());
        }
    }
}
