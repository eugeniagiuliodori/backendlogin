package com.example.demo.Model;

public class UserNotFoundException extends Exception{

    private String msge;

    @Override
    public String toString() {
        return "{UserNotFoundException:" + msge + "}";
    }

    public String getMsge() {
        return msge;
    }

    public void setMsge(String msge) {
        this.msge = msge;
    }
}
