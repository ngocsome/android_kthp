package com.example.android_haui.model;

// Một lần thay đổi trạng thái phòng
public class StatusHistory {

    private int id;
    private String roomCode;
    private String oldStatus;
    private String newStatus;
    private long changedAt; // thời điểm thay đổi (milliseconds)

    public StatusHistory(int id, String roomCode, String oldStatus,
                         String newStatus, long changedAt) {
        this.id = id;
        this.roomCode = roomCode;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
    }

    public int getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public long getChangedAt() {
        return changedAt;
    }
}
