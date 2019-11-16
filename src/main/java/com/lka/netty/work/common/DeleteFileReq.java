package com.lka.netty.work.common;

public class DeleteFileReq extends AbstractMessage {
    String fileToDelete;

    public DeleteFileReq(String fileToDelete) {
        this.fileToDelete = fileToDelete;
    }

    public String getFileToDelete() {
        return fileToDelete;
    }
}
