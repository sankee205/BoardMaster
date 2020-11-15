package com.example.boardmaster;

import java.util.ArrayList;

public class CurrentUser {
    private static CurrentUser instance = null;

    private static User user;
    private static String token;
    private static boolean userLogedIn;
    private static ArrayList<String> gameList;
    private static String group;
    public static CurrentUser getInstance() {
        if (instance == null) {
            instance = new CurrentUser();

        }

        return instance;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        CurrentUser.group = group;
    }

    public ArrayList<String> getGameList() {
        return gameList;
    }

    public void setGameList(ArrayList<String> gameList) {
        CurrentUser.gameList = gameList;
    }

    public void setInstance(CurrentUser instance) {
        CurrentUser.instance = instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        CurrentUser.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        CurrentUser.token = token;
    }

    public boolean isUserLogedIn() {
        return userLogedIn;
    }

    public void setUserLogedIn(boolean userLogedIn) {
        CurrentUser.userLogedIn = userLogedIn;
    }
}
