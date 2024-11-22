package io.hunter.node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class NodeLib {

    public static boolean cycle(NodeWriter output, byte name, OutputStream writer, InputStream reader) throws IOException {
        NetworkFrame frame = FrameLibrary.getNetworkFrame(reader);
        if (frame.getDest() != name) {
            return false;
        }
        if (isAckFrame(frame)) {
            return true;
        }
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
}
