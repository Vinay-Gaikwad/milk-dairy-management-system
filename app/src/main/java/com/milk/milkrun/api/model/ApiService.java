package com.milk.milkrun.api.model;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/insertTrnMilk")
    Call<ApiResponse> sendMilkTransaction(
            @Body TrnMilkModel trnMilkModel
    );
}
