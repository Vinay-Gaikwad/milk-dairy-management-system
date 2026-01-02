package com.milk.milkrun.api.milkrate;

import com.google.gson.annotations.SerializedName;

public class MilkRate {

    @SerializedName("SoftCustCode")
    public String SoftCustCode;

    @SerializedName("RtGrpCode")
    public String RtGrpCode;

    @SerializedName("TrzDate")
    public String TrDate;

    @SerializedName("MlkTypeCode")
    public int MlkTypeCode;

    @SerializedName("Fat")
    public String Fat;

    @SerializedName("SNF")
    public String SNF;

    @SerializedName("Rate")
    public String Rate;
}
