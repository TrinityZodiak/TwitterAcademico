package com.example.twitteracademico.models;

public class Follow {

    private String idSeguidor;
    private String idSiguiendo;
    private String id;
    private Long timestamp;

    public Follow() {
    }

    public Follow(String idSeguidor, String idSiguiendo, String id, Long timestamp) {
        this.idSeguidor = idSeguidor;
        this.idSiguiendo = idSiguiendo;
        this.id = id;
        this.timestamp = timestamp;
    }

    public void setIdSeguidor(String idSeguidor) {
        this.idSeguidor = idSeguidor;
    }

    public void setIdSiguiendo(String idSiguiendo) {
        this.idSiguiendo = idSiguiendo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getIdSeguidor() {
        return idSeguidor;
    }

    public String getIdSiguiendo() {
        return idSiguiendo;
    }
}
