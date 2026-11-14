package com.smartcity360.decorator;


public abstract class DataStreamDecorator implements DataStream {

  
    protected final DataStream wrappedStream;

    protected DataStreamDecorator(DataStream wrappedStream) {
        this.wrappedStream = wrappedStream;
    }

    @Override
    public String readData() {
        return wrappedStream.readData();
    }

    @Override
    public void writeData(String data) {
        wrappedStream.writeData(data);
    }
}

