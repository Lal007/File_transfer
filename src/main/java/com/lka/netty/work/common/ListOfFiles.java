package com.lka.netty.work.common;

import java.util.List;

public class ListOfFiles extends AbstractMessage{
    private List<String> files;

    public ListOfFiles(List<String> files) {
        this.files = files;
    }

    public List<String> getFiles() {
        return files;
    }
}
