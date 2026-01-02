package com.milk.milkrun.api.model;

import com.google.gson.annotations.SerializedName;

public class TrnMilkModel {

    @SerializedName("SoftCustCode")
    public String softCustCode;

    @SerializedName("ATrNo")
    public String atrNo;

    @SerializedName("TrNo")
    public String trNo;

    @SerializedName("TrDate")
    public String trDate;

    @SerializedName("MlkTrType")
    public String mlkTrType;

    @SerializedName("MECode")
    public int meCode; // 1 = Morning, 2 = Evening

    @SerializedName("PartyCode")
    public String partyCode;

    @SerializedName("MlkTypeCode")
    public String mlkTypeCode; // 1 = Buffalo, 2 = Cow

    @SerializedName("Qty")
    public double qty;

    @SerializedName("Fat")
    public double fat;

    @SerializedName("SNF")
    public double snf;

    @SerializedName("Rate")
    public double rate;

    @SerializedName("Amt")
    public double amt;

    // 🔥 New fields for default 0 or empty string values
    @SerializedName("SampNo")
    public String sampNo = "";

    @SerializedName("SrNo")
    public int srNo = 0;

    @SerializedName("Deg")
    public double deg = 0;

    @SerializedName("Water")
    public double water = 0;

    @SerializedName("BlTypeCode")
    public String blTypeCode = "";

    @SerializedName("BrCode")
    public String brCode = "";

    @SerializedName("PstInd")
    public String pstInd = "";

    @SerializedName("SgSdQty")
    public double sgSdQty = 0;

    @SerializedName("SgSdFat")
    public double sgSdFat = 0;

    @SerializedName("SgSdSNF")
    public double sgSdSNF = 0;

    @SerializedName("SgSdRate")
    public double sgSdRate = 0;

    @SerializedName("SgSdAmt")
    public double sgSdAmt = 0;

    @SerializedName("SSMQty")
    public double ssmQty = 0;

    @SerializedName("SSMFat")
    public double ssmFat = 0;

    @SerializedName("SSMSNF")
    public double ssmSNF = 0;

    @SerializedName("SSMRate")
    public double ssmRate = 0;

    @SerializedName("SSMAmt")
    public double ssmAmt = 0;

    @SerializedName("BdQty")
    public double bdQty = 0;

    @SerializedName("BdRate")
    public double bdRate = 0;

    @SerializedName("BdAmt")
    public double bdAmt = 0;

    @Override
    public String toString() {
        return "TrnMilkModel{" +
                "softCustCode='" + softCustCode + '\'' +
                ", atrNo='" + atrNo + '\'' +
                ", trNo='" + trNo + '\'' +
                ", trDate='" + trDate + '\'' +
                ", mlkTrType='" + mlkTrType + '\'' +
                ", meCode=" + meCode +
                ", partyCode='" + partyCode + '\'' +
                ", mlkTypeCode='" + mlkTypeCode + '\'' +
                ", qty=" + qty +
                ", fat=" + fat +
                ", snf=" + snf +
                ", rate=" + rate +
                ", amt=" + amt +
                ", sampNo='" + sampNo + '\'' +
                ", srNo=" + srNo +
                ", deg=" + deg +
                ", water=" + water +
                ", blTypeCode='" + blTypeCode + '\'' +
                ", brCode='" + brCode + '\'' +
                ", pstInd='" + pstInd + '\'' +
                ", sgSdQty=" + sgSdQty +
                ", sgSdFat=" + sgSdFat +
                ", sgSdSNF=" + sgSdSNF +
                ", sgSdRate=" + sgSdRate +
                ", sgSdAmt=" + sgSdAmt +
                ", ssmQty=" + ssmQty +
                ", ssmFat=" + ssmFat +
                ", ssmSNF=" + ssmSNF +
                ", ssmRate=" + ssmRate +
                ", ssmAmt=" + ssmAmt +
                ", bdQty=" + bdQty +
                ", bdRate=" + bdRate +
                ", bdAmt=" + bdAmt +
                '}';
    }
}
