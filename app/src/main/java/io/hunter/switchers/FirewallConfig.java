package io.hunter.switchers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import io.hunter.model.NetworkFrame;

public class FirewallConfig {

    private String fileName;
    private File firewallFile;

    private ArrayList<Byte> blockedNetworks;
    private ArrayList<String> blockedNodes;

    public FirewallConfig() {
        fileName = "firewall/policy.txt";
        firewallFile = new File(fileName);
        blockedNetworks = new ArrayList<>();
        blockedNodes = new ArrayList<>();
    }

    public void parseFirewallFile() {
        try {
            Scanner line = new Scanner(firewallFile);
            while(line.hasNextLine()) {
                String lineStr = line.nextLine();

                Scanner colon = new Scanner(lineStr);
                colon.useDelimiter(":");
                Scanner route = new Scanner(colon.next());
                route.useDelimiter("_");
                String networkStr = route.next();
                String node = route.next();

                byte network = (byte) Integer.parseInt(networkStr);

                if(node.equalsIgnoreCase("#") && colon.next().equalsIgnoreCase("local")) {
                    blockedNetworks.add(network);
                } else {
                    blockedNodes.add(lineStr);
                }
            }
        } catch(IOException e) {
            System.out.println("Error accessing firewall policy file.");
        }
    }

    /**
     * 
     * @param message - Check the message to see if it can be sent or if blocked Ack is sent.
     * @return True if firewall rule exists for communication. False if firewall rule doesnt exist.
     */
    public boolean checkFirewallPolicy(NetworkFrame message) {
        byte checkValue = message.getNetworkDest();
        for(byte blocked : blockedNetworks)
        {
            if (blocked == checkValue && message.getControl() != 1) {
                return true;
            }
        }

        return false;
    }

    public NetworkFrame genPolicyFrame() {
        if(blockedNodes.size() == 0)
            return null;
        
        return null;
    }
}
