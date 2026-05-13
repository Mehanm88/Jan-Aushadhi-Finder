package com.example.janna.util

import java.util.*

object SearchUtils {
    /**
     * Calculates the Levenshtein distance between two strings.
     * This represents the minimum number of single-character edits 
     * (insertions, deletions or substitutions) required to change one word into the other.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val str1 = s1.lowercase(Locale.getDefault())
        val str2 = s2.lowercase(Locale.getDefault())
        
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }

        for (i in 0..str1.length) {
            for (j in 0..str2.length) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    else -> {
                        val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                        dp[i][j] = minOf(
                            dp[i - 1][j] + 1,      // deletion
                            dp[i][j - 1] + 1,      // insertion
                            dp[i - 1][j - 1] + cost // substitution
                        )
                    }
                }
            }
        }
        return dp[str1.length][str2.length]
    }

    /**
     * Checks if the query fuzzy-matches the target string within a given threshold.
     * Also checks if the target contains the query as a substring for better results.
     */
    fun isFuzzyMatch(query: String, target: String, threshold: Int = 2): Boolean {
        if (query.isBlank()) return true
        val q = query.lowercase(Locale.getDefault())
        val t = target.lowercase(Locale.getDefault())
        
        // Direct contains check is often better for partial matches
        if (t.contains(q)) return true
        
        // If query is short, Levenshtein might be too aggressive, check substrings
        val words = t.split(" ", "-", "+")
        for (word in words) {
            if (levenshteinDistance(q, word) <= threshold) return true
        }
        
        return levenshteinDistance(q, t) <= threshold
    }
}
