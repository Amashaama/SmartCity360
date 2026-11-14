package com.smartcity360.decorator;


public class StreamOptions {

    private final boolean compress;
    private final boolean encrypt;
    private final boolean authenticate;
    private final String token;

    public StreamOptions(boolean compress, boolean encrypt, boolean authenticate, String token) {
        this.compress = compress;
        this.encrypt = encrypt;
        this.authenticate = authenticate;
        this.token = token;
    }

    public boolean isCompress() {
        return compress;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public boolean isAuthenticate() {
        return authenticate;
    }

    public String getToken() {
        return token;
    }
}

