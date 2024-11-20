package io.hunter.node;

import io.hunter.model.*;

import java.io.BufferedOutputStream;
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
        fileName = "arm" + network + "/Node" + name + ".txt";
        this.network = network;
        this.name = name;
    }

    public void readFile() {
        configFile = new File(fileName);
        try { 
            FileReader fileReader = new FileReader(configFile);
            Scanner fileLine = new Scanner(fileReader);
            while(fileLine.hasNext()) {
                Scanner scanner = new Scanner(fileLine.nextLine());
                scanner.delimiter(":");
            }
        }
        catch(IOException exception) {
            System.out.println("[Node " + name + ", Network " + network + "] Could not load file at " + configFile.getPath());
        }
    }
}
