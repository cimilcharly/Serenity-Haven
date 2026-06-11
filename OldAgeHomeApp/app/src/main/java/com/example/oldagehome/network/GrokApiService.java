package com.example.oldagehome.network;

import com.example.oldagehome.models.api.ChatRequest;
import com.example.oldagehome.models.api.ChatResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface GrokApiService {
    @POST("v1/chat/completions")
    Call<ChatResponse> getChatCompletion(
            @Header("Authorization") String authorization,
            @Body ChatRequest request);
}
