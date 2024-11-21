package io.hunter.node;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class Node implements Runnable{

    public NodeConfig config;

    public byte network = 1;
    public byte name = 1;

    private Socket socket;

    public Node(byte network, byte name) {
        this.network = network;
        this.name = name;
        config = new NodeConfig(name, network);
        config.readFile();
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        connect();
        try {
            BufferedOutputStream writer = new BufferedOutputStream(socket.getOutputStream());
            while(true) {
                NetworkFrame frame = config.getFrame();
                if(frame == null)
                    break;

                Thread.sleep(1000);
                writer.write(frame.generateFrame());
                writer.flush();
                frame.debugFrame();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 25565);
        } catch (IOException e) {
            System.out.println("[Node "+name+" Network "+network+"] Did not connect u_m_u");
        }
    }
}