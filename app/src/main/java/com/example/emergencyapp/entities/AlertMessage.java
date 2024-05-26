package com.example.emergencyapp.entities;

public class AlertMessage {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String message;
    private String title;
    private long timestamp;
    private boolean isRead;

    public AlertMessage() {
    }

    public AlertMessage(String senderId, String receiverId, String title, String message, long timestamp, boolean isRead) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
