package io.hunter.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.google.common.graph.Network;

public class FrameLibrary {

    public static NetworkFrame getNetworkFrame(BufferedInputStream reader) throws IOException {
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

    public static NetworkFrame getNetworkFrame(Socket socket) throws IOException {
        return getNetworkFrame(new BufferedInputStream(socket.getInputStream()));
    }

    public static void sendNetworkFrame(BufferedOutputStream writer, NetworkFrame frame) throws IOException {
        writer.write(frame.generateFrame());
        writer.flush();
    }

    public static void sendNetworkFrame(Socket socket, NetworkFrame frame) throws IOException {
        sendNetworkFrame(new BufferedOutputStream(socket.getOutputStream()), frame);
    }

    public static void sendBadFrameAck(NetworkFrame frame, BufferedOutputStream writer) throws IOException {
        byte dest = frame.getSrc();
        byte networkDest = frame.getNetworkSource();
        byte src = frame.getDest();
        byte networkSrc = frame.getNetworkDest();
        byte control = 1;
        String messgae = "CORRUPTED";
        
        NetworkFrame newFrame = new NetworkFrame(src, networkSrc, dest, networkDest, control, messgae);
        sendNetworkFrame(writer, newFrame);
    }

    public static void sendFrameAck(NetworkFrame frame, BufferedOutputStream writer) throws IOException {
        NetworkFrame ackFrame = new NetworkFrame(frame.getDest(), frame.getNetworkDest(), frame.getSrc(), frame.getNetworkSource(), (byte) 1, "ACK");
        ackFrame.debugFrame("NODE");
        sendNetworkFrame(writer, ackFrame);
    }
}
