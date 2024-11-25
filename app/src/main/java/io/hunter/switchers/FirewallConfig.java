package io.hunter.switchers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

import io.hunter.model.NetworkFrame;

public class FirewallConfig {

    private String fileName;
    private File firewallFile;

    private ArrayList<Byte> blockedNetworks;
    private ArrayList<String> blockedNodes;

    private Dictionary<Byte, Byte> localPolicies;

    public FirewallConfig() {
        localPolicies= new Hashtable<>();
    }

    public FirewallConfig(String document) {
        fileName = "firewall/" + document + ".txt";
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

    public ArrayList<NetworkFrame> genPolicyFrame() {
        if(blockedNodes.size() == 0)
            return null;
        
        ArrayList<NetworkFrame> policies = new ArrayList<>();
        
        for(String policy: blockedNodes) {
            NetworkFrame frame = new NetworkFrame(
                (byte) 0, //Src is not used.
                (byte) 0, //NetworkSource is not use
                (byte) 0, //Destination is not used
                (byte) 0, //Destination Network is not used
                (byte) 1, //Is a control frame
                policy
            );
            policies.add(frame);
        }
        
        return policies;
    }

    public void parsePolicyFrame(NetworkFrame frame, byte network) {
        //Setup parsing of the incoming policy
        Scanner message = new Scanner(frame.getMessage());
        message.useDelimiter(":");
        Scanner seperator = new Scanner(message.next());
        seperator.useDelimiter("_");
        //Get the network
        byte policyNetwork = (byte) Integer.parseInt(seperator.next());
        //get the node
        byte policyNode = (byte) Integer.parseInt(seperator.next());
        
        //Check if its meant for this node.
        if(network != policyNetwork)
        {
            message.close();
            seperator.close();
            return;
        }
        //Check too see if the policy has local on it.
        if(!message.next().equalsIgnoreCase("local"))
        {
            message.close();
            seperator.close();
            return;
        }

        System.out.println("[Hub " + network + "] Added policy to enforce.");
        
        message.close();
        seperator.close();

        localPolicies.put(policyNode, (byte) 1);
     }

    public boolean checkLocalPolicy(NetworkFrame frame, byte network) {
        if (frame.getNetworkSource() == network)
            return false;
        if (frame.getControl() == 1)
            return false;
        if (localPolicies.get(frame.getDest()) == null)
            return false;
        return true;
    }
}
