package io.hunter.node;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.graph.Network;

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
            update();
        }
        return true;

    }

    public void update() throws IOException {
        NetworkFrame frame = FrameLibrary.getNetworkFrame(node.reader);
        if (frame.getDest() != node.name) {
            return;
        }
        if (isAckFrame(frame)) {
            sent = true;
            return;
        }
        node.nodeOutput.writeFrame(frame);
        FrameLibrary.sendFrameAck(frame, node.writer);
    }

    public boolean isAckFrame(NetworkFrame frame) {
        if (frame.getControl() != 1)
            return false;
        if (!frame.getMessage().equalsIgnoreCase("ack"))
            return false;
        return true;
    }

}
