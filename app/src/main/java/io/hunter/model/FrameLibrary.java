package io.hunter.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FrameLibrary {
    
    /**
     * Reads a network frame from a input stream.
     * 
     * @param reader The input stream.
     * @return The frame it read.
     * @throws IOException Was unable to read on the input stream.
     */
    public static NetworkFrame getNetworkFrame(InputStream reader) throws IOException {
        byte src = reader.readNBytes(1)[0];
        byte srcNetwork = reader.readNBytes(1)[0];
        byte dest = reader.readNBytes(1)[0];
        byte destNetwork = reader.readNBytes(1)[0];
        byte control = reader.readNBytes(1)[0];
        byte crc = reader.readNBytes(1)[0];
        byte size = reader.readNBytes(1)[0];
        byte[] meat = reader.readNBytes((int) size);
        return new NetworkFrame(src, srcNetwork, dest, destNetwork, control, crc, size, meat);
    }

    /**
     * Writes a network frame on a output stream and then flushes the output writer.
     * 
     * @param writer The output stream to write on
     * @param frame The frame to be written
     * @throws IOException Failed to use output stream to write frame.
     */
    public static void sendNetworkFrame(OutputStream writer, NetworkFrame frame) throws IOException {
        writer.write(frame.generateFrame());
        writer.flush();
    }

    /**
     * Sends a corrupted ack to a node if the information is corrupted.
     * 
     * @param frame The frame that was corrupted
     * @param writer The nodes socket writer
     * @throws IOException Was unable to write to nodes socket.
     */
    public static void sendBadFrameAck(NetworkFrame frame, OutputStream writer) throws IOException {
        byte dest = frame.getSrc();
        byte networkDest = frame.getNetworkSource();
        byte src = frame.getDest();
        byte networkSrc = frame.getNetworkDest();
        byte control = 1;
        String messgae = "CORRUPTED";
        
        NetworkFrame newFrame = new NetworkFrame(src, networkSrc, dest, networkDest, control, messgae);
        sendNetworkFrame(writer, newFrame);
    }
    /**
     * Send an acknloedgemnt frame by using the information of the frame received.
     * 
     * @param frame The frame received, to figure out who the ack goes too.
     * @param writer The nodes socket writer.
     * @throws IOException The nodes socket wasunable to be written on.
     */
    public static void sendFrameAck(NetworkFrame frame, OutputStream writer) throws IOException {
        byte control = 1;
        NetworkFrame ackFrame = new NetworkFrame(frame.getDest(), frame.getNetworkDest(), frame.getSrc(), frame.getNetworkSource(), control, "ACK");
        sendNetworkFrame(writer, ackFrame);
    }

    /**
     * 
     * @param frame
     * @return
     */
    public static NetworkFrame sendBlockedFrameAck(NetworkFrame frame) {
        byte control = 1;
        NetworkFrame ackFrame = new NetworkFrame(frame.getDest(), frame.getNetworkDest(), frame.getSrc(), frame.getNetworkSource(), control, "BLOCKED");
        return ackFrame;
    }

    /**
     * Sends a terminating frame from the node to it's hub letting it know the node is done sending messages.
     * 
     * @param network The network byte of the node thats closing out
     * @param name The name of the node closing out.
     * @param writer The nodes socket writer.
     * @throws IOException Failed to send node terminating frame on the nodes hub socket.
     */
    public static void sendTermNodeFrame(byte name, byte network, OutputStream writer) throws IOException {
        NetworkFrame terminating = new NetworkFrame(
            name,
            network,
            (byte) 0,
            (byte) 0,
            (byte) 1,
            "NODE_TERM"
        );
        sendNetworkFrame(writer, terminating);
    }

    /**
     * Sends a terminating frame from the hub to the main hub letting it know the hub is done sending messages.
     * 
     * @param network The network byte of the hub thats finished.
     * @param writer The nodes socket writer.
     * @throws IOException Failed to send hub temrinating frame on main hub socket.
     */
    public static void sendTermNetworkFrame(byte network, OutputStream writer) throws IOException {
        NetworkFrame terminating = new NetworkFrame(
            (byte) 0,
            network,
            (byte) 0,
            (byte) 0,
            (byte) 1,
            "NETWORK_TERM"
        );
        sendNetworkFrame(writer, terminating);
    }

    /**
     * Gives a frame that signals a global termination of the program.
     * @return The network frame encoded with end of all frames being sent.
     */
    public static NetworkFrame genGlobalTerm() {
        NetworkFrame terminating = new NetworkFrame(
            (byte) 0,
            (byte) 0,
            (byte) 0,
            (byte) 0,
            (byte) 1,
            "GLOBAL_TERM"
        );
        return terminating;
    }

    /**
     * Gives a frame that signals the end of the firewall policies have been sent.
     * @return The network frame encoded with end of firewall message
     */
    public static NetworkFrame endOfFirewallFrame() {
        NetworkFrame terminating = new NetworkFrame(
            (byte) 0,
            (byte) 0,
            (byte) 0,
            (byte) 0,
            (byte) 1,
            "END_FIREWALL"
        );
        return terminating;
    }
    
}
