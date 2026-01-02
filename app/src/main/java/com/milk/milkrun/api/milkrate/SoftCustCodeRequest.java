package com.milk.milkrun.api.milkrate;

public class SoftCustCodeRequest {
    private String SoftCustCode;

    public SoftCustCodeRequest(String softCustCode) {
        this.SoftCustCode = softCustCode;
    }

    public String getSoftCustCode() {
        return SoftCustCode;
    }

    public void setSoftCustCode(String softCustCode) {
        this.SoftCustCode = softCustCode;
    }
}
