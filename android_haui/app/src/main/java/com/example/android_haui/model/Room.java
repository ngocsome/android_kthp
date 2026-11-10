package com.example.android_haui.model;

public class Room {

    private int id;
    private String roomCode;
    private int floor;
    private String status;

    public Room(int id, String roomCode, int floor, String status) {
        this.id = id;
        this.roomCode = roomCode;
        this.floor = floor;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public int getFloor() {
        return floor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}