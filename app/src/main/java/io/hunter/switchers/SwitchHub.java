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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        /**
         * First listen for frames from each node.
         * And add them to the queue.
         */

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        for (int i = 0; i < hosts.size() ; i++) {

            
            //TODO At some point implement the ability to time out when receivign data from a node.
                
            ExecutorService executor = Executors.newSingleThreadExecutor();
            ReadNetworkFrame task = new ReadNetworkFrame(readers.get(hosts.get(i)));

            Future<NetworkFrame> future = executor.submit(task);

            try {
                NetworkFrame result = future.get(1, TimeUnit.SECONDS);
                frameQueue.add(result);
                addRoute(result, hosts.get(i));
                result.debugFrame("SWITCH");
            } catch (TimeoutException e) {
                System.out.println("Timeout occurred, skipping this.");
                future.cancel(true); // Interrupt the task if it's still running
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Error occurred: " + e.getMessage());
            } finally {
                executor.shutdown();
            }

            /**
             * Read frame
             */
        }
        /**
         * After getting at least one frame from the nodes then we want to 
         * send a frame if one exists in the queue.
         */
        sendFrame(frameQueue.poll());
    }

    public void addRoute(NetworkFrame frame, Socket socket) {
        routes.put(frame.getSrc(), socket);
    }

    /**
     * This function determines where and how frames should be sent once they are in the Queue.
     * 
     * @param frame the frame we wish to send accross the network.
     */
    public void sendFrame(NetworkFrame frame) {
        if(frame == null)
            return;
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

        /**
         * Attempt to send over the socket.
         */
        try {
            FrameLibrary.sendNetworkFrame(recipient, frame);
        }
        catch (IOException e) {
            System.out.println("Failed to send frame to socket.");
        }
        return false;
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

                readers.put(socket, new BufferedInputStream(socket.getInputStream(), 24000));
                writers.put(socket, new BufferedOutputStream(socket.getOutputStream(), 24000));

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
