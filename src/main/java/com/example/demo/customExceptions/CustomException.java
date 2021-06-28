package com.example.demo.customExceptions;

public class CustomException extends Exception{

    private String name;

    private String description;


    public CustomException(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public CustomException() {
        this.name = new String("");
        this.description = new String("");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "{'error': '** Error **" + name + ":" + description+"'}";
    }
}
