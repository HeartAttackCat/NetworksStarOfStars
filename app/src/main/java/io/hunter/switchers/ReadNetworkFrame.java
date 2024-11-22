package io.hunter.switchers;

import java.io.BufferedInputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.Callable;

import com.google.common.graph.Network;

import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class ReadNetworkFrame implements Callable<NetworkFrame> {

    private BufferedInputStream reader;

    public ReadNetworkFrame(BufferedInputStream reader) {
        this.reader = reader;
    }

    @Override
    public NetworkFrame call() throws Exception {
        NetworkFrame frame = FrameLibrary.getNetworkFrame(reader);
        return frame;
    }
    
}
