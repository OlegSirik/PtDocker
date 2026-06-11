package ru.pt.domain.model;

import ru.pt.api.dto.product.PvVar;

import java.math.BigDecimal;

public final class PolicyCoreView {


    public BigDecimal getCoverSumInsured(VariableContext ctx, String cover) {
        if (cover == null) {
            return null;
        }
        try {
            String sumInsuredVarCode = PvVar.varSumInsured(cover).getVarCode(); //"co_" + cover + "_sumInsured";
            return (BigDecimal) ctx.get(sumInsuredVarCode);
        } catch ( Exception e) {
            return null;
        }
    }

    public BigDecimal getCoverPremium(VariableContext ctx, String cover) {
        if (cover == null) {
            return null;
        }
try {
        String premiumVarCode = PvVar.varPremium(cover).getVarCode(); //"co_" + cover + "_premium";
        return (BigDecimal) ctx.get(premiumVarCode);
    } catch ( Exception e) {
        return null;
    }
}

    public Long getCoverDeductibleNr(VariableContext ctx, String cover) {
        if (cover == null) {
            return null;
        }
try {
        String deductibleNrVarCode = PvVar.varDeductibleNr(cover).getVarCode(); //"co_" + cover + "_deductibleNr";
        String deductibleNr = ctx.getString(deductibleNrVarCode);
        return Long.parseLong( deductibleNr);
    } catch ( Exception e) {
        return null;
    }

    }

    public BigDecimal getCoverLimitMin(VariableContext ctx, String cover) {
        if (cover == null) {
            return null;
        }
        try {
        String limitMinVarCode = PvVar.varLimitMin(cover).getVarCode(); //"co_" + cover + "_limitMin";
        return (BigDecimal) ctx.get(limitMinVarCode);
    } catch ( Exception e) {
        return null;
    }

    }
    public BigDecimal getCoverLimitMax(VariableContext ctx, String cover) {
        if (cover == null) {
            return null;
        }
        try {
        String limitMaxVarCode = PvVar.varLimitMax(cover).getVarCode(); //"co_" + cover + "_limitMin";
        return (BigDecimal)ctx.get(limitMaxVarCode);
    } catch ( Exception e) {
        return null;
    }

    }

    public String getPackageNo(VariableContext ctx) {
        try {
        return  (String) ctx.get("io_packageCode");
    } catch ( Exception e) {
        return null;
    }
    }    
}
