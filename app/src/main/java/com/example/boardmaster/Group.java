package com.example.boardmaster;

public class Group {
    public static final String USER = "user";
    public static final String ADMIN = "admin";
    public static final String[] GROUPS = {USER, ADMIN};


    String name;

    String project;

    public Group(String name) {
        this.name = name;
    }
}
