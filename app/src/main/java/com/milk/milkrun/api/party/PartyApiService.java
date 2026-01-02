package com.milk.milkrun.api.party;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PartyApiService {

    @Headers("Content-Type: application/json")
    @POST("get_mst_party_by_softcustcode")
    Call<MstPartyResponse> getMstPartyBySoftCustCode(@Body Map<String, String> request);

}
