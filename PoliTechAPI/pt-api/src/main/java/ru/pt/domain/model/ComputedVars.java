package ru.pt.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

public class ComputedVars {

    private final VariableContext ctx;

    public ComputedVars(VariableContext ctx) {
        this.ctx = ctx;
    }

    public  String getMagicValue(String key) {
    try {
        PvVarDefinition varDef = ctx.getDefinition(key);
        if (varDef == null) {
            return null;
        }
        switch (key) {
            case "ph_isMale":
                return "M".equals(ctx.getString("ph_gender")) ? "X" : "";            
            case "ph_isFemale":
                return "F".equals(ctx.getString("ph_gender")) ? "X" : "";
            case "ph_age_issue":
                LocalDate birthDate = LocalDate.parse(ctx.getString("ph_birthDate"));
                LocalDate issueDate = LocalDate.parse(ctx.getString("pl_issueDate"));
                return Integer.toString(Period.between(birthDate, issueDate).getYears());
            case "ph_age_end":
                LocalDate birthDatePhEnd = LocalDate.parse(ctx.getString("ph_birthDate"));
                LocalDate endDatePhEnd = LocalDate.parse(ctx.getString("pl_endDate"));
                return Integer.toString(Period.between(birthDatePhEnd, endDatePhEnd).getYears());
            case "io_age_issue":
                LocalDate birthDateIO = LocalDate.parse(ctx.getString("io_birthDate"));
                LocalDate issueDateIO = LocalDate.parse(ctx.getString("pl_issueDate"));
                return Integer.toString(Period.between(birthDateIO, issueDateIO).getYears());
            case "io_age_end":
                LocalDate birthDateIOEnd = LocalDate.parse(ctx.getString("io_birthDate"));
                LocalDate endDateIOEnd = LocalDate.parse(ctx.getString("pl_endDate"));
                return Integer.toString(Period.between(birthDateIOEnd, endDateIOEnd).getYears());
            case "pl_TermMonths":
                LocalDate st = LocalDate.parse(ctx.getString("pl_startDate"));
                LocalDate ed = LocalDate.parse(ctx.getString("pl_endDate"));
                Period p = Period.between(st, ed);
                int m = p.getYears() * 12 + p.getMonths();
                return Integer.toString(m);
            case "pl_TermDays":
                LocalDate startDate = LocalDate.parse(ctx.getString("pl_startDate"));
                LocalDate endDate = LocalDate.parse(ctx.getString("pl_endDate"));
                long days = ChronoUnit.DAYS.between(startDate, endDate);
                return Long.toString(days);
            case "io_legs":
                Object io = ctx.get("io_ticketNr");
                if (io instanceof Collection) {
                    return Integer.toString(((Collection<?>) io).size());
                }
                return "0";
            default:
                return key + " Not Found";
        }
    } catch (Exception e) {
        return "";
    }
}
}

