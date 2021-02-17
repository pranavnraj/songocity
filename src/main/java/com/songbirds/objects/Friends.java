package com.songbirds.objects;

import java.util.ArrayList;
import java.util.List;

public class Friends {

    private List<String> friendIDs;

    public Friends(){
        super();
    }

    public Friends(List<String> friendIDs) {
        this.friendIDs = friendIDs;
    }

    public void setFriendIDs(List<String> friendIDs) {
        this.friendIDs = friendIDs;
    }

    public List<String> getFriendIDs() {
        return friendIDs;
    }
}
