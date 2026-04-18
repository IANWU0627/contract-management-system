package com.contracthub.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Heuristic extraction of performance / obligation time phrases from contract text.
 */
public final class PerformanceMilestonePatterns {

    private static final Pattern SIGN_DAYS_ZH = Pattern.compile(
        "(?:签署|签订|签约)后\\s*(\\d+)\\s*(?:个)?\\s*(日|天|个工作日)");
    private static final Pattern DELIVER_DAYS_ZH = Pattern.compile(
        "(?:交货|交付|发货)后\\s*(\\d+)\\s*(?:个)?\\s*(日|天|个工作日)");
    private static final Pattern ACCEPT_DAYS_ZH = Pattern.compile(
        "验收(?:合格)?后\\s*(\\d+)\\s*(?:个)?\\s*(日|天|个工作日)");
    private static final Pattern WITHIN_DAYS_EN = Pattern.compile(
        "(?i)within\\s+(\\d+)\\s+days?");

    private PerformanceMilestonePatterns() {
    }

    public static List<Extracted> extractAll(String text) {
        List<Extracted> out = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return out;
        }
        matchPattern(out, text, SIGN_DAYS_ZH, "SIGN", "签订/签署后");
        matchPattern(out, text, DELIVER_DAYS_ZH, "DELIVERY", "交付后");
        matchPattern(out, text, ACCEPT_DAYS_ZH, "ACCEPTANCE", "验收后");
        matchPattern(out, text, WITHIN_DAYS_EN, "WITHIN_EN", "Within days");
        return out;
    }

    private static void matchPattern(
        List<Extracted> out,
        String text,
        Pattern p,
        String anchor,
        String label
    ) {
        Matcher m = p.matcher(text);
        while (m.find()) {
            int days = Integer.parseInt(m.group(1));
            String snippet = shortenSnippet(text, m.start(), m.end());
            out.add(new Extracted(label + " " + days + " 日", days, anchor, snippet));
        }
    }

    private static String shortenSnippet(String text, int start, int end) {
        int s = Math.max(0, start - 40);
        int e = Math.min(text.length(), end + 40);
        String raw = text.substring(s, e).replaceAll("\\s+", " ").trim();
        if (raw.length() > 200) {
            return raw.substring(0, 197) + "...";
        }
        return raw;
    }

    public static final class Extracted {
        public final String title;
        public final int offsetDays;
        public final String anchorNote;
        public final String rawSnippet;

        public Extracted(String title, int offsetDays, String anchorNote, String rawSnippet) {
            this.title = title;
            this.offsetDays = offsetDays;
            this.anchorNote = anchorNote;
            this.rawSnippet = rawSnippet;
        }
    }
}
