package com.example.boardmaster.game;

import com.example.boardmaster.Photo;
import com.example.boardmaster.User;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Game {
    Long id;
    String gameName;
    String title;
    String description;
    String date;
    String time;
    int maxPlayers;
    User gameOwner;
    List<Photo> profileImages = new ArrayList<>();

    public List<Photo> getProfileImages() {
        return profileImages;
    }

    public void setProfileImages(List<Photo> profileImages) {
        this.profileImages = profileImages;
    }

    public User getGameOwner() {
        return gameOwner;
    }

    public void setGameOwner(User gameOwner) {
        this.gameOwner = gameOwner;
    }

    ArrayList<User> players;

    public Long getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public ArrayList<User> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<User> players) {
        this.players = players;
    }
}
