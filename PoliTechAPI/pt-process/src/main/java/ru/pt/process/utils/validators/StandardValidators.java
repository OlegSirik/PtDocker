package ru.pt.process.utils.validators;

import java.util.regex.Pattern;

/**
 * Standard validators for common data types like email, phone number, etc.
 */
public class     StandardValidators {

    // Email regex pattern (RFC 5322 compliant, simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Phone number patterns (supports various formats)
    // Russian phone: +7XXXXXXXXXX, 8XXXXXXXXXX, +7 (XXX) XXX-XX-XX, etc.
    private static final Pattern PHONE_RU_PATTERN = Pattern.compile(
        "^(\\+7|8)?[\\s\\-]?\\(?[0-9]{3}\\)?[\\s\\-]?[0-9]{3}[\\s\\-]?[0-9]{2}[\\s\\-]?[0-9]{2}$"
    );

    // International phone pattern (simplified)
    private static final Pattern PHONE_INTERNATIONAL_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );

    // INN (Russian Tax ID) pattern: 10 or 12 digits
    private static final Pattern INN_PATTERN = Pattern.compile("^\\d{10}|\\d{12}$");

    // SNILS (Russian Social Security Number) pattern: XXX-XXX-XXX XX
    private static final Pattern SNILS_PATTERN = Pattern.compile("^\\d{3}-\\d{3}-\\d{3} \\d{2}$");

    // Passport series and number (Russian format): XXXX XXXXXX
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^\\d{4} \\d{6}$");

    /**
     * Validates email address
     * @param email email address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates phone number (Russian format)
     * @param phone phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhoneRu(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleaned = phone.trim().replaceAll("[\\s\\-\\(\\)]", "");
        return PHONE_RU_PATTERN.matcher(phone.trim()).matches() || 
               PHONE_INTERNATIONAL_PATTERN.matcher(cleaned).matches();
    }

    /**
     * Validates phone number (international format)
     * @param phone phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhoneInternational(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleaned = phone.trim().replaceAll("[\\s\\-\\(\\)]", "");
        return PHONE_INTERNATIONAL_PATTERN.matcher(cleaned).matches();
    }

    /**
     * Validates INN (Russian Tax ID)
     * @param inn INN to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidInn(String inn) {
        if (inn == null || inn.trim().isEmpty()) {
            return false;
        }
        return INN_PATTERN.matcher(inn.trim()).matches();
    }

    /**
     * Validates SNILS (Russian Social Security Number)
     * @param snils SNILS to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSnils(String snils) {
        if (snils == null || snils.trim().isEmpty()) {
            return false;
        }
        return SNILS_PATTERN.matcher(snils.trim()).matches();
    }

    /**
     * Validates passport series and number (Russian format)
     * @param passport passport series and number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassport(String passport) {
        if (passport == null || passport.trim().isEmpty()) {
            return false;
        }
        return PASSPORT_PATTERN.matcher(passport.trim()).matches();
    }

    /**
     * Generic validator that routes to appropriate validator based on rule type
     * @param ruleType type of validation rule (EMAIL, PHONE, INN, etc.)
     * @param value value to validate
     * @return true if valid, false otherwise
     */
    public static boolean validate(String ruleType, String value) {
        if (value == null) {
            return false;
        }

        return switch (ruleType.toUpperCase()) {
            case "EMAIL" -> isValidEmail(value);
            case "PHONE", "PHONE_RU" -> isValidPhoneRu(value);
            case "PHONE_INTERNATIONAL" -> isValidPhoneInternational(value);
            case "INN" -> isValidInn(value);
            case "SNILS" -> isValidSnils(value);
            case "PASSPORT" -> isValidPassport(value);
            default -> false;
        };
    }
}
