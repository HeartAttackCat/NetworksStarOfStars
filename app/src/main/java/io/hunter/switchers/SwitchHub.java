package io.hunter.switchers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private Dictionary<Socket, InputStream> readers = new Hashtable<Socket, InputStream>();
    private Dictionary<Socket, OutputStream> writers = new Hashtable<Socket, OutputStream>();

    private Dictionary<Byte, Socket> routes = new Hashtable<Byte, Socket>();

    public SwitchHub(int totalConnections, byte network) {
        /**
         * Setup the switch name.
         */
        this.network = network;
        /**
         * Configure total connections.
         */
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
        /**
         * 
         */
        int available = 0;
        
        for (Socket socket : hosts) {
            try {
                available = socket.getInputStream().available();
                if (available != 0)
                {
                    NetworkFrame frame = FrameLibrary.getNetworkFrame(socket);
                    frameQueue.add(frame);
                    addRoute(frame, socket);
                    sendFrame(frameQueue.poll());
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            
        }
    }

    public void addRoute(NetworkFrame frame, Socket socket) {
        byte src = frame.getSrc();
        if (routes.get(src) != null)
            return;
        
        routes.put(src, socket);
        System.out.println("Adding route for "+  src);
        
    }

    /**
     * This function determines where and how frames should be sent once they are in the Queue.
     * 
     * @param frame the frame we wish to send accross the network.
     */
    public void sendFrame(NetworkFrame frame) {
        if(frame == null)
            return;
        if(sendThruRoute(frame) == true)
            return;
        
        flood(frame);
    }

    public void flood(NetworkFrame frame) {
        for(Socket socket: hosts) {
            try {
                FrameLibrary.sendNetworkFrame(socket, frame);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This function will send the network frame through the route to it's host if we know
     * who the host is.
     * @param frame
     * @return
     */
    public boolean sendThruRoute(NetworkFrame frame) {
        //Get the the destination byte from the frame.
        byte dest = frame.getDest();
        //Grab the socket.
        
        Socket recipient = routes.get(dest);

        if (recipient == null)
            return false;

        try {
            FrameLibrary.sendNetworkFrame(writers.get(recipient), frame);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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

                readers.put(socket, socket.getInputStream());
                writers.put(socket, socket.getOutputStream());

                connected++;
            }
            server.close();

            System.out.println("Switching to transmission.");

        } catch(IOException e) {
            System.out.println("[Switch Network " + network + "]Failed to start Switch");
            System.exit(-3);
        }
    }

    @Override
    public void run() {
        /**
         * Initialize the vairables and other components for our switch.
         */
        start();
        /**
         * Get all incoming connections.
         */
        getConnections();
        
        //Transmission stage of the switch.
        while(transmit) {
            switchMessages();

        }

    }
}
