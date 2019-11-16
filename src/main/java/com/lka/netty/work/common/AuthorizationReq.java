package com.lka.netty.work.common;

public class AuthorizationReq extends AbstractMessage {
    private String login;
    private String pass;

    public AuthorizationReq(String login, String pass) {
        this.login = login;
        this.pass = pass;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }
}
