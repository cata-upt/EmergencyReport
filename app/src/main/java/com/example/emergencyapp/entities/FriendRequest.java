package com.example.emergencyapp.entities;

public class FriendRequest {
    private String userId;
    private String friendId;

    public FriendRequest(String userId, String friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
}

