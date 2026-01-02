package com.milk.milkrun.api2;

import java.util.List;

public class LoginResponse {
    private boolean status;
    private String message;
    private List<LoginData> data;

    public boolean getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<LoginData> getData() {
        return data;
    }

    public static class LoginData {
        private String PartyCode;
        private String PartyNameOth;
        private String Mob;

        public String getPartyCode() {
            return PartyCode;
        }

        public String getPartyNameOth() {
            return PartyNameOth;
        }

        public String getMob() {
            return Mob;
        }
    }
}
