package io.hunter.switchers;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class ReadNetworkFrame implements Callable<NetworkFrame> {

    private InputStream reader;

    public ReadNetworkFrame(InputStream reader) {
        this.reader = reader;
    }

    @Override
    public NetworkFrame call() throws Exception {
        NetworkFrame frame = FrameLibrary.getNetworkFrame(reader);
        return frame;
    }
    
}
