package com.smartcity360.decorator;


public class CompressionDecorator extends DataStreamDecorator {

    
    private static final String SPACE_RUN = "\\s+";
    private static final String SINGLE_SPACE = " ";

    public CompressionDecorator(DataStream wrappedStream) {
        super(wrappedStream);
    }

    @Override
    public void writeData(String data) {
        wrappedStream.writeData(compress(data));
    }


    public static String compress(String text) {
        return text.trim().replaceAll(SPACE_RUN, SINGLE_SPACE);
    }
}

