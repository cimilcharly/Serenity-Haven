package com.example.oldagehome.utils;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MemoryManager {

    private final Context context;
    private final List<String> shortTermMemory; // Last 5 messages
    private final List<JSONObject> knowledgeBase;

    public MemoryManager(Context context) {
        this.context = context;
        this.shortTermMemory = new ArrayList<>();
        this.knowledgeBase = loadKnowledgeBase();
    }

    public void addMessage(String message) {
        shortTermMemory.add(message);
        if (shortTermMemory.size() > 5) {
            shortTermMemory.remove(0); // Truncate oldest
        }
    }

    public String retrieveKnowledge(String query) {
        String queryLower = query.toLowerCase();
        StringBuilder responses = new StringBuilder();

        // Check for specific condition treatments first
        String treatmentSuggestion = suggestTreatment(queryLower);
        if (!treatmentSuggestion.isEmpty()) {
            return treatmentSuggestion;
        }

        for (JSONObject item : knowledgeBase) {
            try {
                String category = item.getString("category");

                if (category.equals("medicines")) {
                    String name = item.getString("name").toLowerCase();
                    if (queryLower.contains(name)) {
                        responses.append(item.getString("name")).append(": ").append(item.getString("usage"))
                                .append("\n");
                        responses.append("Warn: ").append(item.getString("warnings")).append("\n");
                    }
                } else if (category.equals("cures")) {
                    String condition = item.getString("condition").toLowerCase();
                    if (queryLower.contains(condition)) {
                        responses.append("Remedy for ").append(item.getString("condition")).append(": ")
                                .append(item.getString("remedy")).append("\n");
                    }
                } else if (category.equals("health_tips")) {
                    String topic = item.getString("topic").toLowerCase();
                    if (queryLower.contains(topic)) {
                        responses.append("Tip: ").append(item.getString("advice")).append("\n");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return responses.toString();
    }

    public String suggestTreatment(String userQuery) {
        for (JSONObject item : knowledgeBase) {
            try {
                if ("condition_mappings".equals(item.optString("category"))) {
                    JSONArray keywordsArray = item.getJSONArray("keywords");
                    String[] keywords = new String[keywordsArray.length()];
                    for (int i = 0; i < keywordsArray.length(); i++) {
                        keywords[i] = keywordsArray.getString(i);
                    }

                    // Use Recommendation Algorithm to find match
                    double score = RecommendationAlgorithm.keywordMatchScore(userQuery, keywords);
                    if (score > 0.5) {
                        String condition = item.getString("condition");
                        JSONArray medicines = item.getJSONArray("suggested_medicines");
                        String advice = item.getString("advice");

                        StringBuilder result = new StringBuilder();
                        result.append("For **").append(condition).append("**, suggested medicines are:\n");
                        for (int i = 0; i < medicines.length(); i++) {
                            result.append("- ").append(medicines.getString(i)).append("\n");
                        }
                        result.append("\nAdvice: ").append(advice).append("\n");
                        return result.toString();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    // In a real LLM scenario, this would concatenate system prompts + short term
    // memory + retrieved context
    public String buildContext(String currentMessage) {
        StringBuilder context = new StringBuilder();
        context.append("System: Use short answers.\n");
        // Add retrieved knowledge (RAG simulation)
        String knowledge = retrieveKnowledge(currentMessage);
        if (!knowledge.isEmpty()) {
            context.append("Knowledge: ").append(knowledge).append("\n");
        }
        // Add recent history
        for (String msg : shortTermMemory) {
            context.append("History: ").append(msg).append("\n");
        }
        context.append("User: ").append(currentMessage);
        return context.toString();
    }

    private List<JSONObject> loadKnowledgeBase() {
        List<JSONObject> list = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("health_knowledge.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getJSONObject(i));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
