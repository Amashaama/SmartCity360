package com.smartcity360.decorator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class AuthenticationDecorator extends DataStreamDecorator {

    public static final String TOKEN_PREFIX = "TOKEN:";
    public static final String TOKEN_END = "|";
    public static final String WRONG_TOKEN_MESSAGE =
            "Authentication failed: the token does not match";
    private static final String HMAC_SECRET = "UrbanTech-Grid-Auth-Secret";

    private final String token;

    public AuthenticationDecorator(DataStream wrappedStream, String token) {
        super(wrappedStream);
        if (token == null || token.isEmpty() || token.contains(TOKEN_END)) {
            throw new IllegalArgumentException(
                    "Token must not be empty and must not contain '" + TOKEN_END + "'");
        }
        this.token = token;
    }


    public static String generateDeviceToken(String meterId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(HMAC_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal((meterId != null ? meterId : "METER").getBytes(StandardCharsets.UTF_8));
            return "HMAC-" + HexFormat.of().formatHex(rawHmac).substring(0, 12).toUpperCase();
        } catch (Exception e) {
            return "HMAC-GRID-VERIFIED";
        }
    }

    @Override
    public void writeData(String data) {
        wrappedStream.writeData(tokenHeader() + data);
    }

    @Override
    public String readData() {
        String received = wrappedStream.readData();
        String header = tokenHeader();
        if (received == null || !received.startsWith(header)) {
            throw new SecurityException(WRONG_TOKEN_MESSAGE);
        }
        // Token is correct: strip it and return only the real data.
        return received.substring(header.length());
    }

    private String tokenHeader() {
        return TOKEN_PREFIX + token + TOKEN_END;
    }
}


