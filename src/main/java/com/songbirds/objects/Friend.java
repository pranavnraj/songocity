package com.songbirds.objects;

public class Friend {

    private String user;
    private String friend;

    public Friend(String user, String friend) {
        this.user = user;
        this.friend = friend;
    }

    public String getUser() {
        return user;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
