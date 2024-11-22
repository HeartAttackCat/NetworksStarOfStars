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

    public NodeConfig(byte name, byte network) {
        //TODO please update this before submission.
        fileName = "arm" + network + "/Node" + name + ".txt";
        this.network = network;
        this.name = name;
    }

    public NetworkFrame getFrame() {
        return frames.poll();
    }

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
