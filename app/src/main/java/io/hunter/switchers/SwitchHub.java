package io.hunter.switchers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class SwitchHub implements Runnable {

    private int totalConnections = 0, finishedTransmitting = 0;

    private FirewallConfig firewall;

    private int port;

    //All Connections
    private ArrayList<Socket> hosts;

    //Routes to all hubs
    private Dictionary<Byte, Socket> routes;

    //The input streams and output streams.
    private Dictionary<Socket, InputStream> readers;
    private Dictionary<Socket, OutputStream> writers;

    //Boolean for tracking if the
    private boolean transmit = true;

    /**
     * Intiializes the switch hub
     * 
     * @param totalConnections How many hubs under it should connect.
     * @param port The port of the main hub
     */
    public SwitchHub(int totalConnections, int port) {
        this.totalConnections = totalConnections;
        this.port = port;

        Thread thread =new Thread(this);
        thread.start();
    }

    /**
     * This function reads and handles network frames form the hubs.
     */
    public void switchMessages() {
        //Go through all the sockets.
        for (Socket host : hosts) {
            //get the sockets reader and writer.
            InputStream reader = readers.get(host);
            
            //Attempt to read the socket or send frames.
            try {
                //Proceed to handle the socket if it has any information written on it.
                if(reader.available() != 0)
                {
                    NetworkFrame message = FrameLibrary.getNetworkFrame(reader);
                    addRoute(host, message.getNetworkSource());
                    if(firewall.checkFirewallPolicy(message))
                    {
                        sendMessage(FrameLibrary.sendBlockedFrameAck(message));
                    } else {
                        sendMessage(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * This function will decide how to send frames based off of the frame
     * it receives.
     * 
     * @param message The network frame ot be transmitted by the hub.
     */
    public void sendMessage(NetworkFrame message) {
        if(message == null)
            return;
        if(termNetworkMessage(message))
            return;
        if(tryRoute(message, message.getNetworkDest()))
            return;
        
        floodMessage(message);
    }

    /**
     * Check the network frame too see if it is a terminating message.
     * Then handle the accoridng actions if it a terminating frame.
     * 
     * @param frame The network frame to be checked
     * @return true if it is a terminating frame
     */
    public boolean termNetworkMessage(NetworkFrame frame) {
        if(frame.getSrc() != 0 || frame.getDest() != 0 || frame.getNetworkDest() !=0)
            return false;
        //Check too see if it is a Network terminating frame.
        if(!frame.getMessage().equalsIgnoreCase("NETWORK_TERM"))
            return false;
        
        finishedTransmitting++;

        if(finishedTransmitting == totalConnections)
        {
            floodMessage(FrameLibrary.genGlobalTerm());
            System.out.println("[Hub Main] All hubs are done transmitting.");
            transmit = false;
        }
        
        return true;
    }

    /**
     * Add the route for the corresponding socket and byte.
     * 
     * @param socket The Network socket
     * @param src The source byte
     */
    public void addRoute(Socket socket, Byte src) {
        if (routes.get(src) != null)
            return;
        
        routes.put(src, socket);
    }

    /**
     * Will attempt to send via a backwards leanred route.
     * 
     * @param message The message to be sent.
     * @param dest The network destination byte to try route.
     * @return true if it found a route and false if it didn't find a route.
     */
    public boolean tryRoute(NetworkFrame message, byte dest) {
        Socket receiver = routes.get(dest);

        if(receiver == null)
            return false;
        
        try {
            FrameLibrary.sendNetworkFrame(writers.get(receiver), message);
        } catch ( IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Floods a message to all hosts.
     * 
     * @param message The message to be flooded
     */
    public void floodMessage(NetworkFrame message) {
        for (Socket host : hosts) {
            try {
                FrameLibrary.sendNetworkFrame(host, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get connections for the hub of hubs.
     */
    public void getConnections() {
        //Loop through until everything is connected.

        try {
            ServerSocket server = new ServerSocket(port);

            for ( int i = 0; i < totalConnections; i++) {
                

                Socket connection = server.accept();

                hosts.add(connection);

                writers.put(connection, connection.getOutputStream());
                readers.put(connection, connection.getInputStream());

                
            }
            
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Intiializes global instance variables.
     */
    public void init() {
        hosts = new ArrayList<>();
        routes = new Hashtable<>();
        writers = new Hashtable<>();
        readers = new Hashtable<>();
        firewall = new FirewallConfig("policy");
        firewall.parseFirewallFile();
    }

    /**
     * Flood the firewall frame messages to each node and then send terminating frame.
     */
    public void distrobutePolicies() {
        ArrayList<NetworkFrame> policies = firewall.genPolicyFrame();
        
        try{
            for (Socket socket : hosts){
                //Just to make sure there are policies to send.
                if (policies != null) {
                    for(NetworkFrame policy: policies) {
                        FrameLibrary.sendNetworkFrame(writers.get(socket), policy);
                    }
                }
                FrameLibrary.sendNetworkFrame(writers.get(socket), FrameLibrary.endOfFirewallFrame());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        //Intitilization stage.
        init();
        //Get hub connections
        getConnections();
        //Distorbute the policies
        distrobutePolicies();
        //Begin switching frame loop.
        while(transmit) {
            switchMessages();
        }
    }
}
