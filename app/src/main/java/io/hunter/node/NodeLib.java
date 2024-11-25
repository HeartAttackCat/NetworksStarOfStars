package io.hunter.node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class NodeLib {

    /**
     * The function will get a network frame and write it, if it is an acknowledgment
     * it will return true, if it is anything else then a acknowledgement then it will
     * return false.
     * 
     * @param output The node output file to write the messages too.
     * @param name The node's name in the network.
     * @param writer The socket writer for the node.
     * @param reader The socket reader for the node.
     * @param frame The frame we are analyzing.
     * @return true if the message has been handled.
     * @throws IOException If there is an issue with writing a frame to the socket.
     */
    public static boolean cycle(NodeWriter output, byte name, OutputStream writer, InputStream reader, NetworkFrame frame) throws IOException {
        if (frame.getDest() != name && frame.getDest() != 0)
            return false;
        if (!frame.checkCRC())
            return false;
        if (isAckFrame(frame))
            return true;
        if(isBlockAck(frame, output))
            return true;
        output.writeFrame(frame);
        FrameLibrary.sendFrameAck(frame, writer);
        return false;
    }

    /**
     * Checks too see if the frame is an acknowledgement frame
     * 
     * @param frame The frame we wanana check
     * @return Will return true if it is ack frame or false if it is not.
     */
    public static boolean isAckFrame(NetworkFrame frame) {
        if (frame.getControl() != 1)
            return false;
        if (!frame.getMessage().equalsIgnoreCase("ack"))
            return false;
        return true;
    }

    /**
     * Checks too see if the frame is a blocked Acklowedgement
     * then handles it by writing an error to the node output.
     * 
     * @param frame The frame we wanna check
     * @param output The node output file to write too if it is blocked.
     * @return Will return true if it is a blocked frame message and return false if it isnt.
     */
    public static boolean isBlockAck(NetworkFrame frame, NodeWriter output) {
        if (frame.getControl() != 1)
            return false;
        if(!frame.getMessage().equalsIgnoreCase("BLOCKED"))
            return false;
        output.writeBlocked(frame);
        return true;
    }
}
