package ru.pt.api.mapper;

import ru.pt.api.dto.process.Address;
import ru.pt.api.dto.process.ContactInfo;
import ru.pt.api.dto.process.Insurer;
import ru.pt.api.dto.process.Organization;
import ru.pt.api.dto.product.InsuranceCompanyDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сборка {@link Insurer} для полиса из админского {@link InsuranceCompanyDto}.
 */
public final class InsurerMapper {

    private InsurerMapper() {
    }

    public static Insurer fromInsuranceCompany(InsuranceCompanyDto dto) {
        if (dto == null) {
            return null;
        }
        Insurer insurer = new Insurer();
        insurer.setOrganization(toOrganization(dto));
        insurer.setContactInfo(toContactInfo(dto));
        List<Address> addresses = buildAddresses(dto);
        insurer.setAddresses(addresses.isEmpty() ? null : addresses);
        insurer.setIdentifiers(null);
        return insurer;
    }

    /**
     * Только юрлицо (удобно, если в {@code PolicyDTO} пока поле типа {@link Organization}).
     */
    public static Organization toOrganization(InsuranceCompanyDto dto) {
        if (dto == null) {
            return null;
        }
        Organization o = new Organization();
        o.setInn(emptyToNull(dto.getInn()));
        o.setKpp(emptyToNull(dto.getKpp()));
        o.setOgrn(emptyToNull(dto.getOgrn()));
        o.setOkpo(emptyToNull(dto.getOkpo()));
        o.setShortName(emptyToNull(dto.getShortName()));
        String fullName = dto.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = dto.getName();
        }
        o.setFullName(emptyToNull(fullName));
        o.setLegalForm(emptyToNull(dto.getEgr()));

        Map<String, Object> extra = new HashMap<>();
        putIfHasText(extra, "bank", dto.getBank());
        putIfHasText(extra, "bic", dto.getBic());
        putIfHasText(extra, "account", dto.getAccount());
        putIfHasText(extra, "corrAccount", dto.getCorrAccount());
        if (!extra.isEmpty()) {
            o.setAdditionalAttributes(extra);
        }
        return o;
    }

    private static ContactInfo toContactInfo(InsuranceCompanyDto dto) {
        String phone = emptyToNull(dto.getPhone());
        String email = emptyToNull(dto.getMail());
        if (phone == null && email == null) {
            return null;
        }
        ContactInfo c = new ContactInfo();
        c.setPhone(phone);
        c.setEmail(email);
        return c;
    }

    private static List<Address> buildAddresses(InsuranceCompanyDto dto) {
        List<Address> list = new ArrayList<>();
        String legal = emptyToNull(dto.getLegalAddress());
        if (legal != null) {
            Address a = new Address();
            a.setPrimary(true);
            a.setTypeCode("LEGAL");
            a.setAddressStr(legal);
            list.add(a);
        }
        String postal = emptyToNull(dto.getPostalAddress());
        if (postal != null) {
            Address a = new Address();
            a.setPrimary(legal == null);
            a.setTypeCode("POSTAL");
            a.setAddressStr(postal);
            list.add(a);
        }
        return list;
    }

    private static void putIfHasText(Map<String, Object> map, String key, String value) {
        String v = emptyToNull(value);
        if (v != null) {
            map.put(key, v);
        }
    }

    private static String emptyToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s;
    }
}
