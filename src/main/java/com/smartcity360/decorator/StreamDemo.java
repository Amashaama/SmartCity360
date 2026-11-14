package com.smartcity360.decorator;


public final class StreamDemo {

    private StreamDemo() {
     
    }

  
    public static class VerificationResult {
        private final boolean success;
        private final String candidateToken;
        private final String authorizedToken;
        private final String meterId;
        private final String decodedData;
        private final String errorMessage;

        public VerificationResult(boolean success, String candidateToken, String authorizedToken,
                                  String meterId, String decodedData, String errorMessage) {
            this.success = success;
            this.candidateToken = candidateToken;
            this.authorizedToken = authorizedToken;
            this.meterId = meterId;
            this.decodedData = decodedData;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getCandidateToken() {
            return candidateToken;
        }

        public String getAuthorizedToken() {
            return authorizedToken;
        }

        public String getMeterId() {
            return meterId;
        }

        public String getDecodedData() {
            return decodedData;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    
    public static StreamTrace send(String meterId, String reading, StreamOptions options) {
        SmartMeterStream meter = new SmartMeterStream(meterId, reading);
        DataStream stream = StreamBuilder.buildSecureStream(meter, options);

        stream.writeData(reading);
        String finalSent = meter.readData();   
        String readBack = stream.readData();   

      
        String compressed = options.isCompress() ? CompressionDecorator.compress(reading) : reading;
        String encrypted = options.isEncrypt() ? EncryptionDecorator.encrypt(compressed) : compressed;

        return new StreamTrace(options, reading, compressed, encrypted, finalSent, readBack);
    }

    public static VerificationResult verifyToken(String meterId, String reading,
                                                 StreamOptions options, String candidateToken) {
        SmartMeterStream meter = new SmartMeterStream(meterId, reading);

        String authorizedToken = (options != null && options.getToken() != null && !options.getToken().isBlank())
                ? options.getToken() : "secret123";
        boolean compress = options != null && options.isCompress();
        boolean encrypt = options != null && options.isEncrypt();

        
        StreamOptions writerOptions = new StreamOptions(compress, encrypt, true, authorizedToken);
        StreamBuilder.buildSecureStream(meter, writerOptions).writeData(reading);

        
        try {
            if (candidateToken == null || candidateToken.isBlank()) {
                throw new IllegalArgumentException("Passkey token cannot be blank.");
            }
            StreamOptions readerOptions = new StreamOptions(compress, encrypt, true, candidateToken);
            String readBack = StreamBuilder.buildSecureStream(meter, readerOptions).readData();
            return new VerificationResult(true, candidateToken, authorizedToken, meterId, readBack, null);
        } catch (SecurityException e) {
            return new VerificationResult(false, candidateToken, authorizedToken, meterId, null, e.getMessage());
        } catch (IllegalArgumentException e) {
            return new VerificationResult(false, candidateToken, authorizedToken, meterId, null, e.getMessage());
        } catch (Exception e) {
            return new VerificationResult(false, candidateToken, authorizedToken, meterId, null,
                    "Access verification failed: " + e.getMessage());
        }
    }

   
    public static String readWithWrongToken(String meterId, String reading,
                                            StreamOptions options, String wrongToken) {
        VerificationResult result = verifyToken(meterId, reading, options, wrongToken);
        return result.isSuccess() ? "No exception was thrown" : result.getErrorMessage();
    }
}

