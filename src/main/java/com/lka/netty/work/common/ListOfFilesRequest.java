package com.lka.netty.work.common;

public class ListOfFilesRequest extends AbstractMessage {
    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
