package io.hunter.node;

import io.hunter.model.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class NodeConfig {

    private byte name, network;

    private String fileName;

    private File configFile;

    private Queue<NetworkFrame> frames = new LinkedList<NetworkFrame>();

    /**
     * Intializes the node config file.
     * Needs the network and name.
     * 
     * @param name
     * @param network
     */
    public NodeConfig(byte name, byte network) {
        //TODO please update this before submission.
        fileName = "files/Node" + network + "_" + name + ".txt";
        this.network = network;
        this.name = name;
    }

    /**
     * Get the next frame for the node
     * @return will return a frame if one is present or will return null if there is no more frames to send.
     */
    public NetworkFrame getFrame() {
        return frames.poll();
    }

    /**
     * Parse the node config file and populates the frame queue.
     */
    public void readFile() {
        configFile = new File(fileName);
        try { 
            FileReader fileReader = new FileReader(configFile);
            @SuppressWarnings("resource")
            Scanner fileLine = new Scanner(fileReader);
            while(fileLine.hasNext()) {
                @SuppressWarnings("resource")
                Scanner major = new Scanner(fileLine.nextLine());
                major.useDelimiter(":");
                @SuppressWarnings("resource")
                Scanner minor = new Scanner(major.next());
                minor.useDelimiter("_");
                byte networkDest = (byte) Integer.parseInt(minor.next());
                byte dest = (byte) Integer.parseInt(minor.next());
                String message = major.next();
                NetworkFrame frame = new NetworkFrame(name, network, dest, networkDest, (byte) 0, message);
                frames.add(frame);
            }
        }
        catch(IOException exception) {
            System.out.println("[Node " + name + ", Network " + network + "] Could not load file at " + configFile.getPath());
        }
    }
}
