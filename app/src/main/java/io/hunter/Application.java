package io.hunter;

import io.hunter.node.Node;
import io.hunter.switchers.SwitchHub;
import io.hunter.switchers.SwitchNode;

public class Application {

    private static int[] networks = {25565, 2010, 2030, 10800, 10801, 10802, 10803, 10804, 10805, 10806, 10807, 10808, 10809, 10810, 10811, 10812};

    private static int hubs = 3;
    private static int nodes = 2;

    public static void main(String[] args) {
        parseCommandLineArguments(args);
        new SwitchHub(hubs, 7777);
        for (int hub = 1; hub <= hubs; hub++) {
            System.out.println("[Main] Initializing hub " + hub);
            new SwitchNode(nodes, (byte) hub, networks[hub]);

            for (int node = 1; node < nodes + 1; node++) {
                System.out.println("[Main] Initializing node " + node + " for network " + hub);
                new Node((byte) hub, (byte) node, networks[hub]);
            }
        }
    }

    public static void parseCommandLineArguments(String[] args) {
        if (args.length <= 3) {
            System.out.println("[Main] INCORRECT NUMBER OF ARGUMENTS");
            System.exit(-1);
        }
        for (int i = 0; i < args.length; i++)
        {
            if(args[i].equalsIgnoreCase("--node"))
            {
                try {
                    nodes = Integer.parseInt(args[i+1]);
                } catch(Exception e) {
                    System.out.println("[main] Incorrect use of --node argument");
                }
            }
            if(args[i].equalsIgnoreCase("--hub")) {
                try {
                    nodes = Integer.parseInt(args[i+1]);
                } catch(Exception e) {
                    System.out.println("[main] Incorrect use of --hub argument");
                }
            }
        }
    }
}