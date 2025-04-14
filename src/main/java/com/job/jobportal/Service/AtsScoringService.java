package com.job.jobportal.Service;

import org.apache.commons.text.similarity.CosineSimilarity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AtsScoringService {

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "a", "an", "in", "on", "at", "for", "to", "of", "with",
            "is", "are", "was", "were", "be", "been", "being", "have", "has", "had");

    public AtsScoreResult calculateScore(String resumeText, String jobDescription) {
        List<String> resumeTokens = processText(resumeText);
        List<String> jobTokens = processText(jobDescription);

        Set<String> resumeKeywords = new HashSet<>(resumeTokens);
        Set<String> jobKeywords = new HashSet<>(jobTokens);
        Set<String> matchedKeywords = new HashSet<>(resumeKeywords);
        matchedKeywords.retainAll(jobKeywords);

        double keywordMatchScore = calculateKeywordMatchScore(resumeKeywords, jobKeywords, matchedKeywords);
        double cosineSimilarity = calculateCosineSimilarity(resumeTokens, jobTokens);
        double overallScore = (keywordMatchScore * 0.7) + (cosineSimilarity * 0.3);

        return new AtsScoreResult(
                overallScore,
                keywordMatchScore,
                cosineSimilarity,
                new ArrayList<>(matchedKeywords),
                jobKeywords.size());
    }

    private List<String> processText(String text) {
        return Pattern.compile("\\b[a-zA-Z]{3,}\\b")
                .matcher(text.toLowerCase())
                .results()
                .map(match -> match.group())
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.toList());
    }

    private double calculateKeywordMatchScore(Set<String> resumeKeywords,
            Set<String> jobKeywords,
            Set<String> matchedKeywords) {
        if (jobKeywords.isEmpty())
            return 0;
        return (double) matchedKeywords.size() / jobKeywords.size() * 100;
    }

    private double calculateCosineSimilarity(List<String> resumeTokens, List<String> jobTokens) {
        Map<CharSequence, Integer> resumeVector = createFrequencyVector(resumeTokens);
        Map<CharSequence, Integer> jobVector = createFrequencyVector(jobTokens);

        return new CosineSimilarity().cosineSimilarity(resumeVector, jobVector) * 100;
    }

    private Map<CharSequence, Integer> createFrequencyVector(List<String> tokens) {
        Map<CharSequence, Integer> vector = new HashMap<>();
        tokens.forEach(token -> vector.merge(token, 1, Integer::sum));
        return vector;
    }

    public record AtsScoreResult(
            double overallScore,
            double keywordMatchScore,
            double textSimilarityScore,
            List<String> matchedKeywords,
            int totalKeywordsInJobDescription) {
    }
}