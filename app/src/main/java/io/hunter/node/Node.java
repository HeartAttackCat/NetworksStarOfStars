package io.hunter.node;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class Node implements Runnable{

    public NodeConfig config;
    public NodeWriter nodeOutput;

    public byte network;
    public byte name;

    private Socket socket;
    private BufferedOutputStream writer;
    private BufferedInputStream reader;

    private ArrayList<NetworkFrame> messages = new ArrayList<>();

    public Node(byte network, byte name) {
        this.network = network;
        this.name = name;

        config = new NodeConfig(name, network);
        config.readFile();

        nodeOutput = new NodeWriter(network, name);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void start() {
        /**
         * Attempt to initialize our readers and writers.
         */
        
    }

    public void run() {
        /**
         * First grab connection before doing anything. No connection, nothing to do.
         */
        connect();
        /**
         * Intiialize the system and get ready to transmit first frame.
         */
        start();
        if(socket == null || writer == null || reader == null) {
            System.out.println("Was unable to connect");
            return;
        }
        try {
             /**
             * Send first message out.
             */
            transmitMessage();
            /**
             * Begin the switch and transmitting messages.
             */
            while(true) {
                /**
                 * Get frame from switch.
                 */ 
                updateListen();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateListen() throws IOException {
        NetworkFrame frame = FrameLibrary.getNetworkFrame(reader);
        frame.debugFrame("Node " + name);
        if (frame.getDest() != name)
        {
            return;
        }
        if (frame.checkCRC() != true)
        {
            FrameLibrary.sendBadFrameAck(frame, writer);
            return;
        }
        if (isAckFrame(frame) == true) {
            transmitMessage();
            return;
        }
        nodeOutput.writeFrame(frame);
        FrameLibrary.sendFrameAck(frame, writer);
       
    }

    /**
     * 
     * @return
     */
    public boolean isAckFrame(NetworkFrame frame) {
        if (frame.getControl() == 0)
            return false;
        if (!frame.getMessage().equalsIgnoreCase("ACK"))
            return false;
        return true;
    }

    public void transmitMessage() throws IOException {
        NetworkFrame frame = config.getFrame();
        if(frame == null)
            return;
        frame.debugFrame("[Node "+name+"]");
        FrameLibrary.sendNetworkFrame(writer, frame);
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 25565);
            writer = new BufferedOutputStream(socket.getOutputStream());
            reader = new BufferedInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("[Node "+name+" Network "+network+"] Did not connect u_m_u");
        }
    }
}