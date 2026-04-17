package com.contracthub.util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Shared helpers for clause-level contract text comparison (version diff & snapshot diff).
 */
public final class ClauseCompareUtils {

    public static final String CLAUSE_SPLIT_REGEX =
        "(?=(第[一二三四五六七八九十百0-9]+条|[0-9]+\\.|[一二三四五六七八九十]+、))";

    /** Minimum bigram overlap score to treat two blocks as the same clause after reordering. */
    public static final double CONTENT_SIMILARITY_THRESHOLD = 0.45D;

    private static final double MINOR_SIMILARITY_THRESHOLD = 0.96D;

    private ClauseCompareUtils() {
    }

    public static Map<String, String> splitToClauseMap(String content, UnaryOperator<String> normalizeFullText) {
        Map<String, String> result = new LinkedHashMap<>();
        String normalized = normalizeFullText.apply(content == null ? "" : content);
        if (normalized.isBlank()) {
            return result;
        }
        String[] segments = normalized.split(CLAUSE_SPLIT_REGEX);
        int fallbackIndex = 1;
        for (String raw : segments) {
            String segment = raw == null ? "" : raw.trim();
            if (segment.isBlank()) {
                continue;
            }
            String title = extractClauseHeading(segment);
            if (title.isBlank()) {
                title = "条款" + fallbackIndex++;
            }
            result.put(title, segment);
        }
        if (result.isEmpty()) {
            result.put("全文", normalized);
        }
        return result;
    }

    public static String extractClauseHeading(String segment) {
        String s = segment.trim();
        if (s.isBlank()) {
            return "";
        }
        int max = Math.min(24, s.length());
        int end = s.indexOf(' ');
        if (end < 0 || end > max) {
            end = max;
        }
        return s.substring(0, end).replaceAll("[：:，,。].*$", "").trim();
    }

    public static String normalizeTitleKey(String title) {
        return title == null ? "" : title.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", "");
    }

    public static String findMatchingTitle(
        String sourceTitle,
        Map<String, String> targetMap,
        Set<String> matchedTitles
    ) {
        String normalizedSource = normalizeTitleKey(sourceTitle);
        for (String targetTitle : targetMap.keySet()) {
            if (matchedTitles.contains(targetTitle)) {
                continue;
            }
            if (normalizedSource.equals(normalizeTitleKey(targetTitle))) {
                return targetTitle;
            }
        }
        return null;
    }

    public static String findBestContentMatch(
        String sourceContent,
        Map<String, String> targetMap,
        Set<String> matchedTitles,
        UnaryOperator<String> normalizeForLineCompare
    ) {
        String bestTitle = null;
        double bestScore = 0D;
        for (Map.Entry<String, String> target : targetMap.entrySet()) {
            if (matchedTitles.contains(target.getKey())) {
                continue;
            }
            double score = textSimilarityByBigrams(sourceContent, target.getValue(), normalizeForLineCompare);
            if (score > bestScore) {
                bestScore = score;
                bestTitle = target.getKey();
            }
        }
        return bestScore >= CONTENT_SIMILARITY_THRESHOLD ? bestTitle : null;
    }

    public static double textSimilarityByBigrams(
        String textA,
        String textB,
        UnaryOperator<String> normalizeForLineCompare
    ) {
        Set<String> gramsA = buildCharacterBigrams(normalizeForLineCompare.apply(textA));
        Set<String> gramsB = buildCharacterBigrams(normalizeForLineCompare.apply(textB));
        if (gramsA.isEmpty() || gramsB.isEmpty()) {
            return 0D;
        }
        int intersect = 0;
        for (String gram : gramsA) {
            if (gramsB.contains(gram)) {
                intersect++;
            }
        }
        return (double) intersect / (double) Math.max(gramsA.size(), gramsB.size());
    }

    public static boolean isMinorChange(
        String before,
        String after,
        UnaryOperator<String> normalizeForLineCompare,
        UnaryOperator<String> semanticNormalize
    ) {
        String semanticBefore = semanticNormalize.apply(before);
        String semanticAfter = semanticNormalize.apply(after);
        if (semanticBefore.equals(semanticAfter)) {
            return true;
        }
        double similarity = textSimilarityByBigrams(before, after, normalizeForLineCompare);
        int lenA = semanticBefore.length();
        int lenB = semanticAfter.length();
        int gap = Math.abs(lenA - lenB);
        int maxLen = Math.max(lenA, lenB);
        return similarity >= MINOR_SIMILARITY_THRESHOLD && gap <= Math.max(8, maxLen / 20);
    }

    public static String displayRenamedTitle(String leftTitle, String rightTitle) {
        return Objects.equals(leftTitle, rightTitle) ? leftTitle : leftTitle + " -> " + rightTitle;
    }

    private static Set<String> buildCharacterBigrams(String text) {
        Set<String> result = new LinkedHashSet<>();
        String normalized = text == null ? "" : text.replaceAll("\\s+", "");
        if (normalized.length() < 2) {
            return result;
        }
        for (int i = 0; i < normalized.length() - 1; i++) {
            result.add(normalized.substring(i, i + 2));
        }
        return result;
    }
}
