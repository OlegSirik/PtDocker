package ru.pt.domain.model;

import ru.pt.api.dto.product.PvVar;
import ru.pt.api.service.projection.PolicyCoreViewInterface;

import java.math.BigDecimal;

public final class PolicyCoreView implements PolicyCoreViewInterface {

    private final VariableContext ctx;

    public PolicyCoreView(VariableContext ctx) {
        this.ctx = ctx;
    }

    public BigDecimal getCoverSumInsured(String cover) {
        if (cover == null) {
            return null;
        }
        String sumInsuredVarCode = PvVar.varSumInsured(cover).getVarCode(); //"co_" + cover + "_sumInsured";
        return (BigDecimal) this.ctx.get(sumInsuredVarCode);
    }

    public BigDecimal getCoverPremium(String cover) {
        if (cover == null) {
            return null;
        }
        String premiumVarCode = PvVar.varPremium(cover).getVarCode(); //"co_" + cover + "_premium";
        return (BigDecimal) this.ctx.get(premiumVarCode);
    }

    public Long getCoverDeductibleNr(String cover) {
        if (cover == null) {
            return null;
        }
        String deductibleNrVarCode = PvVar.varDeductibleNr(cover).getVarCode(); //"co_" + cover + "_deductibleNr";
        return Long.parseLong( this.ctx.getString(deductibleNrVarCode));
    }

    public BigDecimal getCoverLimitMin(String cover) {
        if (cover == null) {
            return null;
        }
        String limitMinVarCode = PvVar.varLimitMin(cover).getVarCode(); //"co_" + cover + "_limitMin";
        return (BigDecimal) this.ctx.get(limitMinVarCode);
    }
    public BigDecimal getCoverLimitMax(String cover) {
        if (cover == null) {
            return null;
        }
        String limitMaxVarCode = PvVar.varLimitMax(cover).getVarCode(); //"co_" + cover + "_limitMin";
        return (BigDecimal)this.ctx.get(limitMaxVarCode);
    }

    public String getPackageNo() {
        return  (String) this.ctx.get("io_packageCode");
    }    
}
