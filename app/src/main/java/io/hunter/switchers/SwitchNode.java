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

public class SwitchNode implements Runnable {
    
    private boolean transmit = true;

    private byte network;

    private int connected = 0, totalConnections, finishedTransmitting = 0, port = 25565;

    private ArrayList<Socket> hosts = new ArrayList<>();
    private Queue<NetworkFrame> frameQueue = new LinkedList<>();

    private Dictionary<Socket, InputStream> readers = new Hashtable<Socket, InputStream>();
    private Dictionary<Socket, OutputStream> writers = new Hashtable<Socket, OutputStream>();

    private Dictionary<Byte, Socket> routes = new Hashtable<Byte, Socket>();

    private Socket hubSocket;
    private InputStream hubReader;
    private OutputStream hubWriter;

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

        Thread switchMain = new Thread(this);
        switchMain.start();
    }

    /**
     * Switch messages stage of the switch. Will run until all connections have closed out.
     */
    public void switchMessages() {
        /**
         * 
         */
        int available = 0;
        try {
            available = hubSocket.getInputStream().available();
            if (available != 0) {
                NetworkFrame message = FrameLibrary.getNetworkFrame(hubReader);
                if (message.getNetworkDest() == network || message.getNetworkDest() == 0)
                {
                    sendFrame(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
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
        System.out.println("[Hub "+network+"] Adding route for "+  src);
        
    }

    /**
     * This function determines where and how frames should be sent once they are in the Queue.
     * 
     * @param frame the frame we wish to send accross the network.
     */
    public void sendFrame(NetworkFrame frame) {
        if(frame == null)
            return;
        if(termGlobal(frame))
            return;
        if(nodeTermFrame(frame))
            return;
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
        if(frame.getControl() != 1)
            return false;
        if(!frame.getMessage().equalsIgnoreCase("GLOBAL_TERM"))
            return false;

        System.out.println("[Hub "+network+"] got global terminating frame... Stopping transmission." );
        
        flood(frame);

        transmit = false;
        
        return true;
    }

    public boolean nodeTermFrame(NetworkFrame frame) {
        if(frame.getControl() != 1)
            return false;
        if(frame.getNetworkDest() != 0 || frame.getDest() != 0)
            return false;
        if(!frame.getMessage().equalsIgnoreCase("NODE_TERM"))
            return false;
        
        finishedTransmitting++;
        
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
            ServerSocket server = new ServerSocket(port);
            while (connected != totalConnections) {
                Socket socket = server.accept();
                hosts.add(socket);

                readers.put(socket, socket.getInputStream());
                writers.put(socket, socket.getOutputStream());

                connected++;
            }
            server.close();

            System.out.println("[Hub "+network+"] Switching to transmission.");

        } catch(IOException e) {
            System.out.println("[Hub " + network + "] Failed to start Switch");
            System.exit(-3);
        }

        try {
            hubSocket = new Socket("localhost", 7777);
            
            hubReader = hubSocket.getInputStream();
            hubWriter = hubSocket.getOutputStream();
            
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        /**
         * Get all incoming connections.
         */
        getConnections();
        
        //Transmission stage of the switch.
        while(transmit) {
            switchMessages();

        }


        System.out.println("Hub closing down...");
    }
}
