package com.example.oldagehome.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    // Base URL for Groq (Free Tier)
    private static final String BASE_URL = "https://api.groq.com/openai/";

    public static GrokApiService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(GrokApiService.class);
    }
}
