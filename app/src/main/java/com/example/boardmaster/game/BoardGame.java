package com.example.boardmaster.game;

import com.example.boardmaster.Photo;
import com.example.boardmaster.User;

import java.util.List;


public class BoardGame {
    String id;
    String name;
    int players;

    List<Photo> boardImage;

    String boardOwner;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public List<Photo> getBoardImage() {
        return boardImage;
    }

    public void setBoardImage(List<Photo> boardImage) {
        this.boardImage = boardImage;
    }

    public String getBoardOwner() {
        return boardOwner;
    }

    public void setBoardOwner(String boardOwner) {
        this.boardOwner = boardOwner;
    }
}
