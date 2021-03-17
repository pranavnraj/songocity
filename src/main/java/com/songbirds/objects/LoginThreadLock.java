package com.songbirds.objects;

import java.util.ArrayList;
import java.util.List;

public class LoginThreadLock {

    private String state;
    private boolean rdyFlag = false;
    public static List<LoginThreadLock> loginThreadLocks = new ArrayList<LoginThreadLock>();

    public LoginThreadLock(String state) {
        this.state = state;
    }

    public String getStateID() {
        return state;
    }

    public void setStateID(String state) {
        this.state = state;
    }

    public static LoginThreadLock getLoginLock(String state) {
        for (LoginThreadLock lock : loginThreadLocks) {
            if (lock.getStateID().equals(state)) {
                return lock;
            }
        }
        return null;
    }

    public static void addToLoginLocks(LoginThreadLock lock) {
        loginThreadLocks.add(lock);
    }

    public static void removeFromLoginLocks(LoginThreadLock lock) {
        loginThreadLocks.remove(lock);
    }

    public void setRdyFlag() {
        this.rdyFlag = true;
    }

    public boolean isRdyFlag() {
        return rdyFlag;
    }
}
