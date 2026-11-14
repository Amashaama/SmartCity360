package com.smartcity360.decorator;


public final class StreamBuilder {

    private StreamBuilder() {
    }

 
    public static DataStream buildSecureStream(DataStream meter, StreamOptions options) {
        DataStream stream = meter;                  

        if (options.isAuthenticate()) {
            stream = new AuthenticationDecorator(stream, options.getToken());
        }
        if (options.isEncrypt()) {
            stream = new EncryptionDecorator(stream);
        }
        if (options.isCompress()) {
            stream = new CompressionDecorator(stream);
        }
        return stream;
    }
}

