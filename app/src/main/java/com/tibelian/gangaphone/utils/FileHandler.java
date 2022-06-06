package com.tibelian.gangaphone.utils;

import java.io.File;

/**
 * This class is used to assign:
 * a key and a name
 * to a File object
 */
public class FileHandler {

    // file's key
    private String fileKey;

    // file's name
    private String fileName;

    // file object
    private File file;

    // getter
    public String getFileKey() {
        return fileKey;
    }

    // setter
    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    // getter
    public String getFileName() {
        return fileName;
    }

    // setter
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // getter
    public File getFile() {
        return file;
    }

    // setter
    public void setFile(File file) {
        this.file = file;
    }
}
