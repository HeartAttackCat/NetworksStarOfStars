package io.hunter.node;

public class Node implements Runnable{

    public NodeConfig config;

    public byte network = 1;
    public byte name = 1;

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
        
    }
}