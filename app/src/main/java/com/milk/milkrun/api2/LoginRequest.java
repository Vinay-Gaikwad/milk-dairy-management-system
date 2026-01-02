package com.milk.milkrun.api2;

public class LoginRequest {
    private String SoftCustCode;
    private String Mob;

    public LoginRequest(String softCustCode, String mob) {
        this.SoftCustCode = softCustCode;
        this.Mob = mob;
    }
}