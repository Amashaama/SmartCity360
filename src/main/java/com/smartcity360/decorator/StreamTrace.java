package com.smartcity360.decorator;


public class StreamTrace {

    private final StreamOptions options;
    private final String original;
    private final String compressed;
    private final String encrypted;
    private final String finalSent;
    private final String readBack;

    public StreamTrace(StreamOptions options, String original, String compressed,
                       String encrypted, String finalSent, String readBack) {
        this.options = options;
        this.original = original;
        this.compressed = compressed;
        this.encrypted = encrypted;
        this.finalSent = finalSent;
        this.readBack = readBack;
    }

    public StreamOptions getOptions() {
        return options;
    }

    public String getOriginal() {
        return original;
    }

  
    public String getCompressed() {
        return compressed;
    }

    public String getEncrypted() {
        return encrypted;
    }


    public String getFinalSent() {
        return finalSent;
    }

    public String getReadBack() {
        return readBack;
    }
}

