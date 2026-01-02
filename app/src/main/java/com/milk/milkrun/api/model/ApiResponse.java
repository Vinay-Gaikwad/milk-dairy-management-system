// ApiResponse.java
package com.milk.milkrun.api.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    public boolean isSuccessful() {
        return status;
    }
}
