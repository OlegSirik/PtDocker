package ru.pt.process.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

public class VariablesService {

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
