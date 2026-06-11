package com.example.oldagehome.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class RecommendationAlgorithm {

    /**
     * Calculates the Jaccard Similarity between two strings.
     * Jaccard Index = (Intersection of Words) / (Union of Words)
     * Used to match user input with condition keywords.
     */
    public static double calculateJaccardSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null)
            return 0.0;

        Set<String> set1 = new HashSet<>(Arrays.asList(s1.toLowerCase().split("\\s+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(s2.toLowerCase().split("\\s+")));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty())
            return 0.0;

        return (double) intersection.size() / union.size();
    }

    /**
     * Checks if any keyword from the list is present in the input text.
     * Returns a score based on presence (1.0 if highly relevant match, else lower).
     */
    public static double keywordMatchScore(String input, String[] keywords) {
        String lowerInput = input.toLowerCase();
        for (String keyword : keywords) {
            if (lowerInput.contains(keyword.toLowerCase())) {
                return 1.0; // Direct match found
            }
        }
        return 0.0;
    }
}
