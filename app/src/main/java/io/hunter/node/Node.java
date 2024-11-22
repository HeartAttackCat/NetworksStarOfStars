package io.hunter.node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.hunter.model.FrameLibrary;
import io.hunter.model.NetworkFrame;

public class Node implements Runnable{

    public NodeConfig config;
    public NodeWriter nodeOutput;

    public byte network;
    public byte name;

    public Socket socket;
    public OutputStream writer;
    public InputStream reader;

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

            NetworkFrame message = config.getFrame();
            
            while(true) {
                if (message == null)
                    break;
                
                ExecutorService executor = Executors.newSingleThreadExecutor();
                SendNetworkFrame task = new SendNetworkFrame(this, message);

                Future<Boolean> future = executor.submit(task);

                try {
                    Boolean result = future.get(10, TimeUnit.SECONDS);
                    message = config.getFrame();
                } catch (TimeoutException e) {
                    System.out.println("Timeout occurred, resending frame..");
                    future.cancel(true); // Interrupt the task if it's still running
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Error occurred: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    future.cancel(true);
                    executor.shutdown();
                }
            }

            while(true) {
                NetworkFrame incoming = FrameLibrary.getNetworkFrame(reader);
                if (name == incoming.getDest()) {
                    if (incoming.getControl() == 0)
                    {
                        nodeOutput.writeFrame(incoming);
                        FrameLibrary.sendFrameAck(incoming, writer);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void transmitMessage() throws IOException {
        NetworkFrame frame = config.getFrame();
        if(frame == null)
            return;
        FrameLibrary.sendNetworkFrame(writer, frame);
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 25565);
            writer = socket.getOutputStream();
            reader = socket.getInputStream();
        } catch (IOException e) {
            System.out.println("[Node "+name+" Network "+network+"] Did not connect u_m_u");
        }
    }
}