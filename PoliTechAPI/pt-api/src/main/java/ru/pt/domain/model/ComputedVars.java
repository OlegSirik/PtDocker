package ru.pt.domain.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.ZonedDateTime;

public class ComputedVars {

    private static final Logger logger = LoggerFactory.getLogger(ComputedVars.class);

    private ComputedVars() {}

    public static String getMagicValue(VariableContext ctx, String key) {
        logger.trace("getMagicValue called with key: {}", key);
        try {
            PvVarDefinition varDef = ctx.getDefinition(key);
            logger.trace("Variable definition for key '{}': {}", key, varDef != null ? "found" : "not found");
            if (varDef == null) {
                logger.trace("Variable definition not found for key '{}', returning null", key);
                return null;
            }
            
            String result;
            switch (key) {
                case "ph_isMale":
                    String gender = ctx.getString("ph_gender");
                    logger.trace("Computing ph_isMale: gender={}", gender);
                    result = "M".equals(gender) ? "X" : "";
                    logger.trace("ph_isMale result: {}", result);
                    return result;
                    
                case "ph_isFemale":
                    String genderFemale = ctx.getString("ph_gender");
                    logger.trace("Computing ph_isFemale: gender={}", genderFemale);
                    result = "F".equals(genderFemale) ? "X" : "";
                    logger.trace("ph_isFemale result: {}", result);
                    return result;
                    
                case "ph_age_issue":
                    String birthDateStr = ctx.getString("ph_birthDate");
                    String issueDateStr = ctx.getString("pl_issueDate");
                    logger.trace("Computing ph_age_issue: birthDate={}, issueDate={}", birthDateStr, issueDateStr);
                    LocalDate birthDate = getDate(birthDateStr);
                    LocalDate issueDate = getDate(issueDateStr);
                    int age = Period.between(birthDate, issueDate).getYears();
                    result = Integer.toString(age);
                    logger.trace("ph_age_issue result: {} years", age);
                    return result;
                    
                case "ph_age_end":
                    String birthDatePhEndStr = ctx.getString("ph_birthDate");
                    String endDatePhEndStr = ctx.getString("pl_endDate");
                    logger.trace("Computing ph_age_end: birthDate={}, endDate={}", birthDatePhEndStr, endDatePhEndStr);
                    LocalDate birthDatePhEnd = getDate(birthDatePhEndStr);
                    LocalDate endDatePhEnd = getDate(endDatePhEndStr);
                    int ageEnd = Period.between(birthDatePhEnd, endDatePhEnd).getYears();
                    result = Integer.toString(ageEnd);
                    logger.trace("ph_age_end result: {} years", ageEnd);
                    return result;
                    
                case "io_age_issue":
                    String birthDateIOStr = ctx.getString("io_birthDate");
                    String issueDateIOStr = ctx.getString("pl_issueDate");
                    logger.trace("Computing io_age_issue: birthDate={}, issueDate={}", birthDateIOStr, issueDateIOStr);
                    LocalDate birthDateIO = getDate(birthDateIOStr);
                    LocalDate issueDateIO = getDate(issueDateIOStr);
                    int ageIO = Period.between(birthDateIO, issueDateIO).getYears();
                    result = Integer.toString(ageIO);
                    logger.trace("io_age_issue result: {} years", ageIO);
                    return result;
                    
                case "io_age_end":
                    String birthDateIOEndStr = ctx.getString("io_birthDate");
                    String endDateIOEndStr = ctx.getString("pl_endDate");
                    logger.trace("Computing io_age_end: birthDate={}, endDate={}", birthDateIOEndStr, endDateIOEndStr);
                    LocalDate birthDateIOEnd = getDate(birthDateIOEndStr);
                    LocalDate endDateIOEnd = getDate(endDateIOEndStr);
                    int ageIOEnd = Period.between(birthDateIOEnd, endDateIOEnd).getYears();
                    result = Integer.toString(ageIOEnd);
                    logger.trace("io_age_end result: {} years", ageIOEnd);
                    return result;
                    
                case "pl_TermMonths":
                    String startDateStr = ctx.getString("pl_startDate");
                    String endDateStr = ctx.getString("pl_endDate");
                    logger.trace("Computing pl_TermMonths: startDate={}, endDate={}", startDateStr, endDateStr);
                    LocalDate st = getDate(startDateStr);
                    LocalDate ed = getDate(endDateStr).plusDays(1);
                    Period p = Period.between(st, ed);
                    int m = p.getYears() * 12 + p.getMonths();
                    result = Integer.toString(m);
                    logger.trace("pl_TermMonths result: {} months", m);
                    return result;
                    
                case "pl_TermDays":
                    String startDateDaysStr = ctx.getString("pl_startDate");
                    String endDateDaysStr = ctx.getString("pl_endDate");
                    logger.trace("Computing pl_TermDays: startDate={}, endDate={}", startDateDaysStr, endDateDaysStr);
                    LocalDate startDate = getDate(startDateDaysStr);
                    LocalDate endDate = getDate(endDateDaysStr);
                    long days = ChronoUnit.DAYS.between(startDate, endDate);
                    result = Long.toString(days);
                    logger.trace("pl_TermDays result: {} days", days);
                    return result;
                /*     
                case "io_legs":
                    Object io = ctx.get("io_ticketNr");
                    logger.trace("Computing io_legs: io_ticketNr type={}, value={}", 
                        io != null ? io.getClass().getSimpleName() : "null", io);
                    if (io instanceof Collection) {
                        int size = ((Collection<?>) io).size();
                        result = Integer.toString(size);
                        logger.trace("io_legs result: {} (collection size)", size);
                        return result;
                    }
                    logger.trace("io_legs result: 0 (not a collection)");
                    return "0";
                */    
                default:
                    logger.warn("Unknown computed variable key: '{}', returning 'Not Found' message", key);
                    return null;
            }
        } catch (Exception e) {
            logger.error("Error computing magic value for key '{}': {}", key, e.getMessage(), e);
            logger.trace("Exception details for key '{}'", key, e);
            return null;
        }
    }

    public static LocalDate getDate(String isoDateTime) {
        if (isoDateTime == null) {
            return null;
        }
        
        try {
            // Пробуем как ZonedDateTime (с часовым поясом)
            return ZonedDateTime.parse(isoDateTime).toLocalDate();
        } catch (Exception e) {
            // Если не получилось, пробуем как LocalDate (просто дата)
            return LocalDate.parse(isoDateTime);
        }
    }
}

