package com.milk.milkrun.api.milkrate;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("index.php/api/get_milk_rate_by_softcustcode")
    Call<MilkRateResponse> getMilkRates(@Body SoftCustCodeRequest request);

    // Add other API methods as needed
}
