package com.example.oldagehome.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oldagehome.R;
import com.example.oldagehome.adapters.ChatAdapter;
import com.example.oldagehome.models.ChatMessage;
import com.example.oldagehome.models.api.ChatRequest;
import com.example.oldagehome.models.api.ChatResponse;
import com.example.oldagehome.models.api.Message;
import com.example.oldagehome.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private com.example.oldagehome.utils.MemoryManager memoryManager;

    // TODO: Replace with your actual Groq API Key
    // If you don't have one, get it from https://console.groq.com/
    // Format should be: "Bearer gsk_..."
    private static final String API_KEY = "Bearer YOUR_GROQ_API_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
        memoryManager = new com.example.oldagehome.utils.MemoryManager(this);

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        // Initial Greeting
        addBotMessage(
                "Hello! I am your AI Health Assistant powered by Groq. Ask me about your medicines, simple home cures, or just chat with me!");

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String msg = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(msg))
            return;

        // Add User Message
        messageList.add(new ChatMessage(msg, true, System.currentTimeMillis()));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
        etMessage.setText("");

        // Add to memory
        memoryManager.addMessage("User: " + msg);

        // Fetch AI Response
        fetchGrokResponse(msg);
    }

    private void addBotMessage(String message) {
        messageList.add(new ChatMessage(message, false, System.currentTimeMillis()));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }

    private void fetchGrokResponse(String userMessage) {
        // Retrieve context from memory if available
        String context = memoryManager.retrieveKnowledge(userMessage);

        // System Prompt: Give persona to the AI
        String systemPrompt = "You are a helpful, empathetic, and patient health assistant for an old age home named 'Serenity Haven'. "
                +
                "Your users are elderly residents. Use simple, respectful, and warm language. " +
                "Keep answers concise and easy to read. " +
                "If the user asks about medical advice, provide general home remedies but always advise consulting a doctor for serious issues.";

        if (!context.isEmpty()) {
            systemPrompt += "\n\nRelevant Context from database: " + context;
        }

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));
        messages.add(new Message("user", userMessage));

        // Create Request. Switch to 'llama-3.3-70b-versatile' as others may be
        // decomissioned.
        ChatRequest request = new ChatRequest("llama-3.3-70b-versatile", messages);

        // Make API Call
        RetrofitClient.getService().getChatCompletion(API_KEY, request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getChoices() != null
                        && !response.body().getChoices().isEmpty()) {
                    String aiResponse = response.body().getChoices().get(0).getMessage().getContent();

                    runOnUiThread(() -> {
                        addBotMessage(aiResponse);
                        memoryManager.addMessage("Bot: " + aiResponse);
                    });
                } else {
                    // Log error for debugging
                    android.util.Log.e("ChatbotError", "Response Error: " + response.code() + " " + response.message());
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("ChatbotError", "Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String finalError = errorBody;
                    runOnUiThread(() -> {
                        // Fallback to local heuristic if API fails (e.g., invalid key)
                        String heuristic = getLocalHeuristicResponse(userMessage);
                        addBotMessage(heuristic + "\n\n[System: Groq API Error " + response.code() + " - " + finalError
                                + "]");
                        memoryManager.addMessage("Bot: " + heuristic);
                    });
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                android.util.Log.e("ChatbotError", "Network Failure: " + t.getMessage());
                t.printStackTrace();
                runOnUiThread(() -> {
                    String heuristic = getLocalHeuristicResponse(userMessage);
                    addBotMessage(heuristic + "\n\n[System: Network Fail - " + t.getMessage() + "]");
                    memoryManager.addMessage("Bot: " + heuristic);
                });
            }
        });
    }

    private String getLocalHeuristicResponse(String input) {
        input = input.toLowerCase();
        // Try to retrieve knowledge local only
        String knowledge = memoryManager.retrieveKnowledge(input);
        if (!knowledge.isEmpty()) {
            return "Based on my medical database:\n" + knowledge;
        }

        if (input.contains("hello") || input.contains("hi")) {
            return "Hello! How are you feeling today?";
        } else if (input.contains("medicine") || input.contains("tablet") || input.contains("pill")) {
            return "Please check your 'My Medicines' section for your specific schedule. Generally, take medicines after food unless prescribed otherwise. Do you have pain?";
        } else if (input.contains("headache")) {
            return "For a mild headache, drink plenty of water and rest in a dark room. Valid home cures involve ginger tea or a cold compress. If it persists, please consult the doctor.";
        } else if (input.contains("stomach") || input.contains("pain")) {
            return "Stomach pain can be due to acidity or indigestion. Try drinking warm water or mint tea. Avoid spicy food.";
        } else if (input.contains("cold") || input.contains("flu")) {
            return "Stay hydrated and rest. Warm soup and steam inhalation can help. If you have fever, please alert the staff immediately.";
        } else if (input.contains("lonely") || input.contains("sad")) {
            return "I'm here for you. You are part of the Serenity family! Would you like to read a book or listen to some music?";
        } else if (input.contains("thank")) {
            return "You're welcome! Stay healthy.";
        } else {
            return "I am unable to connect to the cloud right now. Can you ask about medicines or health tips?";
        }
    }
}
