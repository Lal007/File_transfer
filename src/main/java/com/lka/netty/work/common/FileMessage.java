package com.lka.netty.work.common;

import java.io.IOException;

public class FileMessage extends AbstractMessage {
    private String filename;
    private byte[] data;
    private int len;

    public static final int MSG_BYTE_BUFFER = 1024 * 1024;

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public FileMessage(String filename, byte[] data, int len) throws IOException {
        this.filename = filename;
        this.data = data;
        this.len = len;
    }

    public int getLen() {
        return len;
    }
}
