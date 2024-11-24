package io.hunter.node;

import java.io.IOException;
import java.util.concurrent.Callable;


import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class SendNetworkFrame implements Callable<Boolean>{

    private Node node;

    private boolean sent = false;

    private NetworkFrame message;

    public SendNetworkFrame(Node node, NetworkFrame message) {
        this.node = node;
        this.message = message;
    }

    @Override
    public Boolean call() throws Exception {
        FrameLibrary.sendNetworkFrame(node.writer, message);
        while (!sent) {
            NetworkFrame frame = FrameLibrary.getNetworkFrame(node.reader);
            sent = NodeLib.cycle(node.nodeOutput, node.name, node.writer, node.reader, frame);
        }
        return true;

    }

    public boolean isAckFrame(NetworkFrame frame) {
        if (frame.getControl() != 1)
            return false;
        if (!frame.getMessage().equalsIgnoreCase("ack"))
            return false;
        return true;
    }

}
