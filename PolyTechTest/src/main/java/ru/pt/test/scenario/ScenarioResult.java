package ru.pt.test.scenario;

public record ScenarioResult(
        String id,
        String title,
        boolean passed,
        long durationMs,
        Integer httpStatus,
        String error
) {
}
