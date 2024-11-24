package io.hunter.node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.hunter.model.NetworkFrame;

public class NodeWriter {

    private File outputFile;
    private String fileName;
    private FileWriter writer;

    public NodeWriter(byte network, byte name) {
        fileName = "arm" + network + "/Node" + name + "Output.txt";
        outputFile = new File(fileName);

        try {
        writer = new FileWriter(outputFile);
        } catch(IOException e) {
            System.out.println("[ERROR] Node " + name + " on network " + network + "was unable to make file writer.");
        }
    }

    public void writeBlocked(NetworkFrame frame) {
        try {
            writer.write("Attempted to message network " + frame.getNetworkSource() + ", node " + frame.getSrc() + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFrame(NetworkFrame frame) {
        String line = 
        frame.getNetworkSource() + "_" +
        frame.getSrc() + ":" +
        frame.getMessage();
        try {
            writer.write(line + "\n");
            writer.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
