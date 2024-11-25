package io.hunter.node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.hunter.model.NetworkFrame;

public class NodeWriter {

    private File outputFile;
    private String fileName;
    private FileWriter writer;

    /**
     * This initializes the node output file writer object.
     * 
     * @param network The network name of the node
     * @param name The nodes name in the network
     */
    public NodeWriter(byte network, byte name) {
        fileName = "files/Node" + network + "_" + name + "Output.txt";
        outputFile = new File(fileName);

        try {
        writer = new FileWriter(outputFile);
        } catch(IOException e) {
            System.out.println("[ERROR] Node " + name + " on network " + network + "was unable to make file writer.");
        }
    }

    /**
     * Write a blocked frame
     * 
     * @param frame The frame that was blocked, will write an error.
     */
    public void writeBlocked(NetworkFrame frame) {
        try {
            writer.write("Attempted to message network " + frame.getNetworkSource() + ", node " + frame.getSrc() +" but was blocked by firewall.\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a frame into the output of the node.
     * 
     * @param frame The frame to be written.
     */
    public void writeFrame(NetworkFrame frame) {
        String line = 
        frame.getNetworkSource() + "_" +
        frame.getSrc() + ":" +
        frame.getMessage();
        try {
            writer.write(line + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}