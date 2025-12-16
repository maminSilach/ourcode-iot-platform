package com.example.apiorchestrator.utils;


public enum FileExtension {
    XML(".xml"),
    JSON(".json");

    private final String extension;

    FileExtension(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
