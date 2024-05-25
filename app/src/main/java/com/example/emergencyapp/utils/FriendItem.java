package com.example.emergencyapp.utils;

public class FriendItem {
    String friendId,name,profileImageUrl,message;
    boolean unreadMessages;
    long timestamp;
    public FriendItem(String friendId, String name, String profileImageUrl,String lastMessage, long timestamp, boolean unreadMessages) {
        this.friendId=friendId;
        this.name= name;
        this.profileImageUrl=profileImageUrl;
        this.message=lastMessage;
        this.timestamp = timestamp;
        this.unreadMessages = unreadMessages;
    }

    public FriendItem() {
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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

    public boolean getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(boolean hasUnreadMessages) {
        this.unreadMessages = hasUnreadMessages;
    }
}
