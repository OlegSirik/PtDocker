package ru.pt.process.utils;

import ru.pt.api.dto.process.Cover;
import ru.pt.api.dto.process.Deductible;
import ru.pt.api.dto.product.PvCover;
import ru.pt.api.dto.product.PvDeductible;
import ru.pt.api.dto.product.PvLimit;
import ru.pt.api.utils.JsonProjection;
import ru.pt.api.dto.product.VarDataType;
import ru.pt.api.dto.product.PvVar;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.pt.api.dto.product.PvVar;
import java.util.Map;

public class VariablesService {


    public static PvLimit getPvLimit(PvCover pvCover, Double sumInsured) {

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
        //String deductibleType = policyCover.getDeductibleType();
        //String deductibleSpecific = policyCover.getDeductibleSpecific();
        //String deductibleUnit = policyCover.getDeductibleUnit();

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

    public static String getMagicValue(List<PvVar> varDefs, String key, String policy) {
        JsonProjection projection = new JsonProjection(policy);

        PvVar varDef;
        try {
            switch (key) {
                case "ph_isMale":
                    varDef = varDefs.stream()
                            .filter(v -> v.getVarCode().equals("ph_gender"))
                            .findFirst()
                            .orElse(null);
                    if (varDef != null) {
                        return "M".equals(varDef.getVarValue()) ? "X" : "";
                    }
                    return "";
                case "ph_isFemale":
                    varDef = varDefs.stream()
                            .filter(v -> v.getVarCode().equals("ph_gender"))
                            .findFirst()
                            .orElse(null);
                    if (varDef != null) {
                        return "F".equals(varDef.getVarValue()) ? "X" : "";
                    }
                    return "";
                case "ph_age_issue":

                    varDef = varDefs.stream()
                            .filter(v -> v.getVarCode().equals("ph_birthdate"))
                            .findFirst()
                            .orElse(null);
                    if (varDef != null) {
                        LocalDate birthDate = LocalDate.parse(varDef.getVarValue());
                        LocalDate issueDate = projection.getIssueDate().toLocalDate();
                        return Integer.toString(Period.between(birthDate, issueDate).getYears());
                    } else {
                        return null;
                    }
                case "io_age_issue":
                    try {
                        varDef = varDefs.stream()
                                .filter(v -> v.getVarCode().equals("io_birthDate"))
                                .findFirst()
                                .orElse(null);
                        if (varDef != null) {
                            return Integer.toString(
                                    Period.between(
                                            LocalDate.parse(varDef.getVarValue()), projection.getIssueDate().toLocalDate()
                                    ).getYears()
                            );
                        }
                    } catch (Exception e) {
                        return "-1";
                    }
                    return "";
                case "io_age_end":
                    try {
                        varDef = varDefs.stream()
                                .filter(v -> v.getVarCode().equals("io_birthDate"))
                                .findFirst()
                                .orElse(null);
                        if (varDef != null) {
                            return Integer.toString(
                                    Period.between(
                                                    LocalDate.parse(varDef.getVarValue()), projection.getEndDate().toLocalDate())
                                            .getYears()
                            );
                        }
                        return "-1";
                    } catch (Exception e) {
                        return "-1";
                    }
                case "pl_TermMonths":
                    LocalDate st = projection.getStartDate().toLocalDate();
                    LocalDate ed = projection.getEndDate().toLocalDate();
                    Period p = Period.between(st, ed);
                    int m = p.getYears() * 12 + p.getMonths();
                    return Integer.toString(m);
                case "pl_TermDays":
                    LocalDate startDate = projection.getStartDate().toLocalDate();
                    String st11 = startDate.toString();
                    LocalDate endDate = projection.getEndDate().toLocalDate();
                    st11 = endDate.toString();
                    long days = ChronoUnit.DAYS.between(startDate, endDate);
                    
                    st11 = Long.toString(days);
                    
                    return Long.toString(days);
    
                default:
                    return key + " Not Found";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static List<PvVar> addVar(List<PvVar> vars, PvVar newVar) {
        // if newVar.getVarCode is not in vars, add newVar to vars
        if (!vars.stream().anyMatch(v -> v.getVarCode().equals(newVar.getVarCode()))) {
            vars.add(newVar);
        }
        return vars;
    }

    public static List<PvVar> addPackageNo(List<PvVar> vars, String val) {
        PvVar var = new PvVar();
        var.setVarCode("pl_packageNo");
        var.setVarName("Package No");
        var.setVarPath("$.packageNo");
        var.setVarType("VAR");
        var.setVarValue(val);
        var.setVarDataType(VarDataType.STRING);
        return addVar(vars, var);
    }

    public static List<PvVar> addProduct(List<PvVar> vars, String val) {
        PvVar var = new PvVar();
        var.setVarCode("pl_product");
        var.setVarName("Product");
        var.setVarPath("$.product");
        var.setVarType("VAR");
        var.setVarValue(val);
        var.setVarDataType(VarDataType.STRING);
        return addVar(vars, var);
    }
    public static String getPackageNo(List<PvVar> vars) {
        // find var with code "pl_packageNo" and return var.getVarValue
        PvVar var = vars.stream()
                .filter(v -> v.getVarCode().equals("pl_packageNo"))
                .findFirst()
                .orElse(null);
        if (var != null) {
            return var.getVarValue();
        }
        return null;
    }


    public static String getComplexField(Map<String, Object> values, String mask) {

        StringBuilder resultMask = new StringBuilder(mask);
        // Replace {KEY} patterns
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(mask);

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement;

            replacement = values.getOrDefault(key, "").toString();

            String target = matcher.group(0);
            int idx;
            while ((idx = resultMask.indexOf(target)) != -1) {
                resultMask.replace(idx, idx + target.length(), replacement);
            }
        }

        return resultMask.toString();
    }

    public static String getPhDigest(String phType, Map<String, Object> vars) {
        if ("person".equals(phType)) {
            return getComplexField(vars, "{ph_firstName} {ph_lastName}");
        } else if ("organization".equals(phType)) {
            return getComplexField(vars, "{ph_org_fullName}");
        }
        return "";
    }
    public static String getIoDigest(String ioType, Map<String, Object> vars) {
        if ("person".equals(ioType)) {
            return getComplexField(vars, "{io_firstName} {io_lastName}");
        } else if ("device".equals(ioType)) {
            return getComplexField(vars, "{io_device_name}");
        }
        return "";
    }
}
