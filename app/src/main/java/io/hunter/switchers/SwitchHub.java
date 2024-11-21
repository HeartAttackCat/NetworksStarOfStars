package io.hunter.switchers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
  
import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class SwitchHub implements Runnable {
    
    private boolean transmit = true;

    private byte network;

    private int connected = 0, totalConnections;

    private ArrayList<Socket> hosts;
    private Queue<NetworkFrame> frameQueue = new LinkedList<>();

    private Dictionary<Socket, BufferedInputStream> readers = new Hashtable<Socket, BufferedInputStream>();
    private Dictionary<Socket, BufferedOutputStream> writers = new Hashtable<Socket, BufferedOutputStream>();

    private Dictionary<Byte, Socket> routes = new Hashtable<Byte, Socket>();

    public SwitchHub(int totalConnections, byte network) {
        this.network = network;
        this.totalConnections = totalConnections;

        Thread switchMain = new Thread(this);
        switchMain.start();
    }

    /**
     * Intiialize all the variables and perform setup for switch.
     */
    public void start() {
        hosts = new ArrayList<Socket>();
    }

    /**
     * Switch messages stage of the switch. Will run until all connections have closed out.
     */
    public void switchMessages() {
        try {
            for (Socket socket : hosts) {
                System.out.println("Listening in");
                NetworkFrame frame = FrameLibrary.getNetworkFrame(readers.get(socket));
                System.out.println("Listening in 2");
                addRoute(frame, socket);

                
                System.out.print("[SWITCH Network "+ network +"]");
                frameQueue.add(frame);
            }

            sendFrame();
        } catch(IOException e) {
            System.out.println("[Switch Network "+ network +"] Unable to read socket.");
        }

    }

    public void addRoute(NetworkFrame frame, Socket socket) {
        routes.put(frame.getSrc(), socket);
    }

    public void sendFrame() {
        
    }

    /**
     * Method for getting all the connections.
     */
    public void getConnections () {
        try {
            ServerSocket server = new ServerSocket(25565);
            while (connected != totalConnections) {
                Socket socket = server.accept();
                hosts.add(socket);

                readers.put(socket, new BufferedInputStream(socket.getInputStream()));
                writers.put(socket, new BufferedOutputStream(socket.getOutputStream()));

                connected++;
            }

            System.out.println("Switching to transmission.");

        } catch(IOException e) {
            System.out.println("[Switch Network " + network + "]Failed to start Switch");
            System.exit(-3);
        }
    }

    @Override
    public void run() {
        start();
        //Get all the nodes connected. Wait for them to join.
        
        getConnections();
        
        //Transmission stage of the switch.
        while(transmit) {
            switchMessages();

        }

    }
}
