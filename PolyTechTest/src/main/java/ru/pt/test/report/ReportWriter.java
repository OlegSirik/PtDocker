package ru.pt.test.report;

import ru.pt.test.scenario.ScenarioResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportWriter {

    private final Path reportDir;

    public ReportWriter(Path reportDir) {
        this.reportDir = reportDir;
    }

    public Path write(List<ScenarioResult> results) throws IOException {
        Files.createDirectories(reportDir);
        String stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        Path md = reportDir.resolve("summary-" + stamp + ".md");
        Path junit = reportDir.resolve("junit-" + stamp + ".xml");
        Path latestMd = reportDir.resolve("summary.md");
        Path latestJunit = reportDir.resolve("junit.xml");

        String markdown = toMarkdown(results);
        String xml = toJunitXml(results);
        Files.writeString(md, markdown, StandardCharsets.UTF_8);
        Files.writeString(junit, xml, StandardCharsets.UTF_8);
        Files.writeString(latestMd, markdown, StandardCharsets.UTF_8);
        Files.writeString(latestJunit, xml, StandardCharsets.UTF_8);
        return latestMd;
    }

    private static String toMarkdown(List<ScenarioResult> results) {
        long passed = results.stream().filter(ScenarioResult::passed).count();
        long failed = results.size() - passed;
        StringBuilder sb = new StringBuilder();
        sb.append("# PolyTechTest regression report\n\n");
        sb.append("- Generated: ").append(Instant.now()).append('\n');
        sb.append("- Total: ").append(results.size()).append('\n');
        sb.append("- Passed: ").append(passed).append('\n');
        sb.append("- Failed: ").append(failed).append("\n\n");
        sb.append("| Status | Id | Title | ms | HTTP | Error |\n");
        sb.append("|--------|----|-------|----|------|-------|\n");
        for (ScenarioResult r : results) {
            sb.append("| ").append(r.passed() ? "PASS" : "FAIL")
                    .append(" | ").append(esc(r.id()))
                    .append(" | ").append(esc(r.title()))
                    .append(" | ").append(r.durationMs())
                    .append(" | ").append(r.httpStatus() == null ? "-" : r.httpStatus())
                    .append(" | ").append(esc(r.error() == null ? "" : r.error()))
                    .append(" |\n");
        }
        return sb.toString();
    }

    private static String toJunitXml(List<ScenarioResult> results) {
        long failures = results.stream().filter(r -> !r.passed()).count();
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<testsuite name=\"PolyTechTest\" tests=\"")
                .append(results.size())
                .append("\" failures=\"")
                .append(failures)
                .append("\" time=\"")
                .append(results.stream().mapToLong(ScenarioResult::durationMs).sum() / 1000.0)
                .append("\">\n");
        for (ScenarioResult r : results) {
            sb.append("  <testcase classname=\"ru.pt.test\" name=\"")
                    .append(xml(r.id()))
                    .append("\" time=\"")
                    .append(r.durationMs() / 1000.0)
                    .append("\"");
            if (r.passed()) {
                sb.append("/>\n");
            } else {
                sb.append(">\n    <failure message=\"")
                        .append(xml(r.error() == null ? "failed" : r.error()))
                        .append("\"/>\n  </testcase>\n");
            }
        }
        sb.append("</testsuite>\n");
        return sb.toString();
    }

    private static String esc(String s) {
        return s.replace("|", "\\|").replace("\n", " ");
    }

    private static String xml(String s) {
        return s.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
