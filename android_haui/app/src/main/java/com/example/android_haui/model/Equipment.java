package com.example.android_haui.model;

// Thiết bị trong phòng học
public class Equipment {

    private int id;
    private String roomCode;
    private String name;
    private int quantity;
    private String condition; // Constants.GOOD hoặc Constants.BROKEN

    public Equipment(int id, String roomCode, String name, int quantity, String condition) {
        this.id = id;
        this.roomCode = roomCode;
        this.name = name;
        this.quantity = quantity;
        this.condition = condition;
    }

    public int getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
