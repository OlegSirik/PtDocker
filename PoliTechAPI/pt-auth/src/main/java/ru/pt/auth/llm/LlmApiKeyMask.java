package ru.pt.auth.llm;

public final class LlmApiKeyMask {

    private LlmApiKeyMask() {
    }

    public static boolean isOmittedOrMasked(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return true;
        }
        String trimmed = apiKey.trim();
        return "***".equals(trimmed) || trimmed.contains("***");
    }

    public static String mask(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        if (apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 3) + "***" + apiKey.substring(apiKey.length() - 4);
    }
}
