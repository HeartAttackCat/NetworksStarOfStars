package io.hunter.node;

import java.util.concurrent.Callable;


import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class SendNetworkFrame implements Callable<Boolean>{

    private Node node;

    private boolean sent = false;

    private NetworkFrame message;

    /**
     * Intializes the object
     * @param node The node object
     * @param message The frame to transmit
     */
    public SendNetworkFrame(Node node, NetworkFrame message) {
        this.node = node;
        this.message = message;
    }
    
    @Override
    /**
     * This call will keep trying to read frames until it gets its ack back for the frame
     * it sent.
     */
    public Boolean call() throws Exception {
        FrameLibrary.sendNetworkFrame(node.writer, message);
        while (!sent) {
            NetworkFrame frame = FrameLibrary.getNetworkFrame(node.reader);
            sent = NodeLib.cycle(node.nodeOutput, node.name, node.writer, node.reader, frame);
        }
        return true;
    }

}
