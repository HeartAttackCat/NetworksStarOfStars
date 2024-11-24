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

    private int totalConnections = 0, finishedTransmitting = 0, connected;

    private FirewallConfig config;

    private int port;

    //All Connections
    private ArrayList<Socket> hosts;

    //Routes to all hubs
    private Dictionary<Byte, Socket> routes;

    //The input streams and output streams.
    private Dictionary<Socket, InputStream> readers;
    private Dictionary<Socket, OutputStream> writers;

    private boolean transmit = true;

    //Start Hub of Hubs thread.
    public SwitchHub(int totalConnections, int port) {
        this.totalConnections = totalConnections;
        this.port = port;

        Thread thread =new Thread(this);
        thread.start();
    }

    public void switchMessages() {
        for (Socket host : hosts) {
            InputStream reader = readers.get(host);
            OutputStream writer = writers.get(host);

            try {
                int available = reader.available();
                if(reader.available() != 0)
                    {
                        NetworkFrame message;
                        try {
                            message = FrameLibrary.getNetworkFrame(reader);
                            addRoute(host, message.getNetworkSource());
                            if(config.checkFirewallPolicy(message))
                            {
                                sendMessage(FrameLibrary.sendBlockedFrameAck(message));
                            } else {
                                sendMessage(message);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(NetworkFrame message) {
        if(message == null)
            return;
        if(termNetworkMessage(message))
            return;
        if(tryRoute(message, message.getNetworkDest()))
            return;
        
        floodMessage(message);
    }

    public boolean termNetworkMessage(NetworkFrame frame) {
        if(frame.getSrc() != 0 || frame.getDest() != 0 || frame.getNetworkDest() !=0)
            return false;
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

    public void addRoute(Socket socket, Byte src) {
        if (routes.get(src) != null)
            return;
        
        routes.put(src, socket);
    }

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

    public void floodMessage(NetworkFrame message) {
        for (Socket host : hosts) {
            try {
                FrameLibrary.sendNetworkFrame(host, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    public void init() {
        hosts = new ArrayList<>();
        routes = new Hashtable<>();
        writers = new Hashtable<>();
        readers = new Hashtable<>();
        config = new FirewallConfig();
        config.parseFirewallFile();
    }

    @Override
    public void run(){
        //Intitilization stage.
        init();
        //Get hub connections
        getConnections();
        
        //Begin switching frame loop.
        //TODO make sure to add terminating sequence.
        while(transmit) {
            switchMessages();
        }
    }
}
