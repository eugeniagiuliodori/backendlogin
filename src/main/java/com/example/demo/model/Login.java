package com.example.demo.model;

public class Login {

    private String userName;

    private String userPass;

    private String clientId;

    private String clientPass;

    private String grantType;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientPass() {
        return clientPass;
    }

    public void setClientPass(String clientPass) {
        this.clientPass = clientPass;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }
}
