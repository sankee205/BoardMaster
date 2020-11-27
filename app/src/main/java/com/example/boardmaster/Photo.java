package com.example.boardmaster;

import java.io.File;

public class Photo {
    String id;
    String filename;
    File file;

    public Photo() {
    }

    public Photo(String id) {
        this.id = id;
    }

    public Photo(File file) {
        this.file = file;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename != null ? filename : file != null ? file.getName() : "default";
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
