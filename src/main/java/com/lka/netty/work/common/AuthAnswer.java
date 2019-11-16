package com.lka.netty.work.common;

public class AuthAnswer extends AbstractMessage{
    private boolean authOK;

    public AuthAnswer(boolean authOK) {
        this.authOK = authOK;
    }

    public boolean isAuthOK() {
        return authOK;
    }
}
