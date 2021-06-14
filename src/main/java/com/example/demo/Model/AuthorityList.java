package com.example.demo.Model;

import com.example.demo.Entity.ERole;

import java.util.LinkedList;

public class AuthorityList extends LinkedList<String> {

    public String toString(){
        String str = new String("");
        for (String grant: this){
            str = str + "," + grant;
        }
        return str;
    }
}
