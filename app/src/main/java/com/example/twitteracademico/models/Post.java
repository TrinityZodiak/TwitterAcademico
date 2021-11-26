package com.example.twitteracademico.models;

public class Post {

    private String id;
    private String title;
    private String description;
    private String idUser;
    private String image1;
    private String image2;

    public Post() {

    }

    public Post(String id, String title, String description, String idUser, String image1, String image2) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.idUser = idUser;
        this.image1 = image1;
        this.image2 = image2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }
}
