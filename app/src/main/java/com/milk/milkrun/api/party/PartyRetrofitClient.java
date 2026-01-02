package com.milk.milkrun.api.party;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PartyRetrofitClient {
    private static final String BASE_URL = "https://api.sanadeinfotech.in/index.php/api/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
