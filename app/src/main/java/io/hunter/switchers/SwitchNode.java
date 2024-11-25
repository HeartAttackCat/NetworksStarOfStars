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

public class SwitchNode implements Runnable {
    
    private FirewallConfig firewall;

    private boolean transmit = true;
    private byte network;

    private int connected = 0, totalConnections, finishedTransmitting = 0, port = 25565;

    //The sockets of all connected nodes.
    private ArrayList<Socket> hosts;

    //The readers and writers for the nodes.
    private Dictionary<Socket, InputStream> readers;
    private Dictionary<Socket, OutputStream> writers;

    //The routes of nodes
    private Dictionary<Byte, Socket> routes;

    /**
     * The sock, read, and writr for the main hub.
     */
    private Socket hubSocket;
    private InputStream hubReader;
    private OutputStream hubWriter;

    /**
     * Constructor for the hub. It also initiates the hubs main thread.
     * 
     * @param totalConnections - total amount of nodes connected to the hub
     * @param network - The name of the network the hub is controlling
     * @param port - The port the hub uses to connect to the nodes.
     */
    public SwitchNode(int totalConnections, byte network, int port) {
        /**
         * Setup the switch name.
         */
        this.network = network;
        /**
         * Configure total connections.
         */
        this.totalConnections = totalConnections;
        /**
         * Configure the network port.
         */
        this.port = port;
        /**
         * Main thread.
         */
        Thread switchMain = new Thread(this);
        switchMain.start();
    }

    /**
     * Switch messages stage of the switch. Will run until all connections have closed out.
     */
    public void switchMessages() {
        /**
         * Get messages from the main hub and distrobute them.
         */
        int available = 0;
        try {
            available = hubSocket.getInputStream().available();
            if (available != 0) {
                NetworkFrame message = FrameLibrary.getNetworkFrame(hubReader);
                if (message.getNetworkDest() == network || message.getNetworkDest() == 0)
                {
                    if(firewall.checkLocalPolicy(message, network))
                    {
                        sendFrame(FrameLibrary.sendBlockedFrameAck(message));
                    } else {
                        sendFrame(message);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        /**
         * Get messages from nodes and distrubute them.
         */
        for (Socket socket : hosts) {
            try {
                available = socket.getInputStream().available();
                if (available != 0)
                {
                    NetworkFrame frame = FrameLibrary.getNetworkFrame(socket);
                    addRoute(frame, socket);
                    sendFrame(frame);
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Add the route for the corresponding socket and byte.
     * 
     * @param socket The Network socket
     * @param src The source byte
     */
    public void addRoute(NetworkFrame frame, Socket socket) {
        byte src = frame.getSrc();
        if (routes.get(src) != null)
            return;
        
        routes.put(src, socket);
        System.out.println("[Hub "+network+"] Adding route for "+  src);
        
    }

    /**
     * This function determines where and how frames should be sent once they are in the Queue.
     * 
     * @param frame the frame we wish to send accross the network.
     */
    public void sendFrame(NetworkFrame frame) {
        //Check too see if the frame is null
        if(frame == null)
            return;
        //Check to see if its a global terminating frame.
        if(termGlobal(frame))
            return;
        //Check to see if its a node terminating frame.
        if(nodeTermFrame(frame))
            return;
        //Check the network destination, if it isn't for this network
        //then dump the frame from the centeral hub if it isn't meant for
        //this network.
        if(frame.getNetworkDest() != network) {
            try {
                FrameLibrary.sendNetworkFrame(hubWriter, frame);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if(tryRoute(frame) == true)
            return;
        
        flood(frame);
    }

    public boolean termGlobal(NetworkFrame frame) {
        //check if its control frame.
        if(frame.getControl() != 1)
            return false;
        //Check if the message is global term which means the centeral hub is done.
        if(!frame.getMessage().equalsIgnoreCase("GLOBAL_TERM"))
            return false;

        System.out.println("[Hub "+network+"] got global terminating frame... Stopping transmission." );
        
        //Flood global termnating frame to children nodes to close out.
        flood(frame);

        transmit = false;
        
        return true;
    }

    /**
     * Check to see if frame is node terminating frame.
     * If it is a node term frame then handle it.
     * 
     * @param frame The frame to check
     * @return true if ter frame and false if not a term frame
     */
    public boolean nodeTermFrame(NetworkFrame frame) {
        //Check to see if its a control frame.
        if(frame.getControl() != 1)
            return false;
        //Check network destination and destination.
        if(frame.getNetworkDest() != 0 || frame.getDest() != 0)
            return false;
        //If message is node term then close out.
        if(!frame.getMessage().equalsIgnoreCase("NODE_TERM"))
            return false;
        
        //Increment the numbers of finished nodes.
        finishedTransmitting++;
        
        //If all nodes are finished let the centeral hub know.
        if(finishedTransmitting == totalConnections) {
            System.out.println("[Hub "+network+"] Finished transmission.");
            try {
                FrameLibrary.sendTermNetworkFrame(network, hubWriter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[Hub "+network+"] Node "+frame.getSrc()+" done transmitting.");
        return true;
    }

    /**
     * Flood the network frame to all connected hosts besides the main hub.
     * 
     * @param frame The frame to be flooded
     */
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
    public boolean tryRoute(NetworkFrame frame) {
        //Get the the destination byte from the frame.
        byte dest = frame.getDest();

        //Grab the socket.
        Socket recipient = routes.get(dest);

        //If route doesn't exist, return false
        if (recipient == null)
            return false;

        try {
            //Try to send through route
            FrameLibrary.sendNetworkFrame(writers.get(recipient), frame);
            return true;
        }
        catch (IOException e) {
            //Failed to send through route
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method for getting all the connections.
     */
    public void getConnections () {
        try {
            //Open port for the hub to operate on.
            ServerSocket server = new ServerSocket(port);

            //Get the nodes before progressing into the next stages.
            while (connected != totalConnections) {
                Socket socket = server.accept();
                hosts.add(socket);
                readers.put(socket, socket.getInputStream());
                writers.put(socket, socket.getOutputStream());

                connected++;
            }
            server.close();

            System.out.println("[Hub "+network+"] Finished getting connections from nodes.");

        } catch(IOException e) {
            System.out.println("[Hub " + network + "] Failed to start Switch");
            System.exit(-3);
        }

        //Connect to the centeral hub.
        try {
            //set the centeral hub socket.
            hubSocket = new Socket("localhost", 7777);
            
            //Generate output and input streams for the application.
            hubReader = hubSocket.getInputStream();
            hubWriter = hubSocket.getOutputStream();
            
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function will talk to the main hub and get policies from it.
     */
    public void getPolicies() {
        //Boolean for main loop, essentially should we keep listening for firewall policie messages.
        boolean listening = true;

        try {
            while(listening) {
                NetworkFrame frame = FrameLibrary.getNetworkFrame(hubReader);
                //Check too see if firewall is doen sending messages.
                if (frame.getMessage().equalsIgnoreCase("END_FIREWALL")) {
                    System.out.println("[Hub "+network+"] Recevied all firewall policies.");
                    listening = false;
                }
                else {
                    //If it is stil lsending messages kjust add the frame to the current policy.
                    System.out.println("[Hub "+network+"] Recevied firewall policy.");
                    firewall.parsePolicyFrame(frame, network);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes global instance variables.
     * Also initializes the firewall config.
     */
    public void init() {
        hosts = new ArrayList<>();

        readers = new Hashtable<Socket, InputStream>();
        writers = new Hashtable<Socket, OutputStream>();

        routes = new Hashtable<Byte, Socket>();

        firewall = new FirewallConfig();
    }

    @Override
    public void run() {
        /**
         * Initialize global instance variables.
         */
        init();
        /**
         * Get all incoming connections.
         */
        getConnections();
        /**
         * Get firewall policies from the centeral hub.
         */
        getPolicies();

        //Check to see if hub should be transmitting and receiving messages.
        while(transmit) {
            switchMessages();
        }
        System.out.println("[Hub " + network + "] Hub closing down...");
    }
}
