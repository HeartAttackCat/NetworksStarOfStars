package io.hunter.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FrameLibrary {

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

    public static NetworkFrame getNetworkFrame(Socket socket) throws IOException {
        return getNetworkFrame(socket.getInputStream());
    }

    public static void sendNetworkFrame(OutputStream writer, NetworkFrame frame) throws IOException {
        writer.write(frame.generateFrame());
        writer.flush();
    }

    public static void sendNetworkFrame(Socket socket, NetworkFrame frame) throws IOException {
        sendNetworkFrame(socket.getOutputStream(), frame);
    }

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

    public static void sendFrameAck(NetworkFrame frame, OutputStream writer) throws IOException {
        byte control = 1;
        NetworkFrame ackFrame = new NetworkFrame(frame.getDest(), frame.getNetworkDest(), frame.getSrc(), frame.getNetworkSource(), control, "ACK");
        sendNetworkFrame(writer, ackFrame);
    }
}
