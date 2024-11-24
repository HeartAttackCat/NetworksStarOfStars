package io.hunter.node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class NodeLib {

    public static boolean cycle(NodeWriter output, byte name, OutputStream writer, InputStream reader, NetworkFrame frame) throws IOException {
        if (frame.getDest() != name && frame.getDest() != 0) {
            return false;
        }
        if (isAckFrame(frame)) {
            return true;
        }
        if(isBlockAck(frame, output))
            return true;
        output.writeFrame(frame);
        FrameLibrary.sendFrameAck(frame, writer);
        return false;
    }

    public static boolean isAckFrame(NetworkFrame frame) {
        if (frame.getControl() != 1)
            return false;
        if (!frame.getMessage().equalsIgnoreCase("ack"))
            return false;
        return true;
    }

    public static boolean isBlockAck(NetworkFrame frame, NodeWriter output) {
        if (frame.getControl() != 1)
            return false;
        if(!frame.getMessage().equalsIgnoreCase("BLOCKED"))
            return false;
        output.writeBlocked(frame);
        return true;
    }
}
