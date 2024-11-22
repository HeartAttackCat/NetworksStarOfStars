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

    public void run() {
        /**
         * First grab connection before doing anything. No connection, nothing to do.
         */
        connect();
        /**
         * Make sure everything setup correctly.
         */
        if(socket == null || writer == null || reader == null) {
            System.out.println("Was unable to connect");
            return;
        }
        /**
         * Begin the major stages of the node.
         */
        try {

            //Get the first message node wishes to send.
            NetworkFrame message = config.getFrame();
            
            
            while(true) {
                //Exit transmitting mode once there are no more frames to transmit.
                if (message == null)
                    break;

                //When in transmitting mode its important to make sure that the message has a time out associated with it.
                ExecutorService executor = Executors.newSingleThreadExecutor();
                //Make a new task that will keep sending the frame until an Ackloedgemnt is heard.
                SendNetworkFrame task = new SendNetworkFrame(this, message);

                //Make a future object and execute.
                Future<Boolean> future = executor.submit(task);

                try {
                    //Try to get the object in 10 seconds.
                    future.get(10, TimeUnit.SECONDS);
                    //Get next frame ready if it was able to transmit sucsessfully.
                    message = config.getFrame();
                } 
                //If it did not get it in 10 seconds dont do anything and let loop execute again to send it.
                catch (TimeoutException e) {
                    System.out.println("Timeout occurred, resending frame..");
                    future.cancel(true); // Interrupt the task if it's still running
                }
                //Just in case if a inttruption or an execution error happens
                catch (InterruptedException | ExecutionException e) {
                    System.out.println("Error occurred: " + e.getMessage());
                    e.printStackTrace();
                } 
                finally {
                    //Finally end the task and the executor object.
                    future.cancel(true);
                    executor.shutdown();
                }
            }

            /**
             * Move into the next stage, which is lsitening until the switch says to close.
             */

            while(true) {
                /**
                 * Node lib helps unify the cycle process for interrpreting the messages since its virtually the same cycle that
                 * SendNetworkFrame object uses.
                 */
                NodeLib.cycle(nodeOutput, name, writer, reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup the connection.
     */
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