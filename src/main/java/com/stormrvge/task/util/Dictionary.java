package com.stormrvge.task.util;

import com.atlassian.jira.issue.Issue;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Dictionary {

    // This function takes list with issues, count repeats of words
    // and puts words and number in linked hashmap, then sorts by descending
    // and return a sorted linked hashmap with 100 most frequently words
    static public Map<String, Integer> countWords(List<Issue> issues) {
        Map<String, Integer> wordsMap = new LinkedHashMap<>();

        for (Issue issue : issues) {
            String summary = issue.getSummary();
            String description = issue.getDescription();

            String[] words = (summary + " " + description).split("\\s+");
            for (int j = 0; j < words.length; j++) {
                words[j] = words[j].replaceAll("^[0-9!@#$%^&*()_\\-=\"]|(?!^)[^a-zA-Zа-яА-Я]", "")
                        .toLowerCase().trim();

                if (words[j].length() == 0) continue;

                if (wordsMap.containsKey(words[j])) {
                    Integer value = wordsMap.get(words[j]);
                    wordsMap.replace(words[j], ++value);
                } else {
                    wordsMap.put(words[j], 1);
                }
            }
        }

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>(100);
        wordsMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(100)
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

        return sortedMap;
    }
}
