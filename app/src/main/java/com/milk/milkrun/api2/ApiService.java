package com.milk.milkrun.api2;

import com.google.gson.JsonObject;
import com.milk.milkrun.api2.LoginRequest;
import com.milk.milkrun.api2.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("get_party_details")
    Call<LoginResponse> getPartyDetails(@Body LoginRequest request);

}